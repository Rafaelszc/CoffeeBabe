import { Button } from "@/components/ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { signInSchema } from "@/schemas/userSchema"
import { Link, useLocation, useNavigate } from "react-router-dom"
import { signInUser } from "@/api/userService"
import { useAuth } from "@/context/auth"
import { AuthCard } from "@/components/site/AuthCard"
import { FormField } from "@/components/site/FormField"
import { toast } from "sonner"


export const Form = () => {
  const { register, handleSubmit, resetField, formState: { errors, isSubmitting } } = useForm({resolver: zodResolver(signInSchema)})
  const navigate = useNavigate()
  const location = useLocation()
  const { refreshUser } = useAuth()

  const onSubmit = async (data) => {
    try {
      await signInUser(data)
      await refreshUser()
      navigate(location.state?.from ?? "/", { replace: true })
    } catch (error) {
      if (error.status === 401) toast.error("E-mail ou senha inválidos.")
      else if (error.status === 429) toast.error("Muitas tentativas. Aguarde alguns minutos e tente de novo.")
      else toast.error("Não foi possível entrar agora. Tente novamente.")
      resetField("password")
    }
  }

  return (
    <AuthCard title="Entrar" subtitle="Acesse sua conta para fazer pedidos.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <FormField id="email" label="E-mail" type="email" autoComplete="email" registration={register("email")} error={errors.email?.message} />
        <FormField id="password" label="Senha" type="password" autoComplete="current-password" registration={register("password")} error={errors.password?.message} />
        <Button className="mt-2 w-full" size="lg" type="submit" disabled={isSubmitting}>Entrar</Button>
        <p className="mt-3 text-center text-sm text-muted-foreground">
          Ainda não tem conta? <Link to="/signup" className="font-medium text-primary underline-offset-4 hover:underline">Criar conta</Link>
        </p>
      </form>
    </AuthCard>
  )
}
