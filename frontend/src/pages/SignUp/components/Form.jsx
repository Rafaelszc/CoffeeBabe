import { Button } from "@/components/ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { signUpSchema } from "@/schemas/userSchema"
import { Link, useNavigate } from "react-router-dom"
import { signUpUser } from "@/api/userService"
import { AuthCard } from "@/components/site/AuthCard"
import { FormField } from "@/components/site/FormField"
import { toast } from "sonner"


export const Form = () => {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({resolver: zodResolver(signUpSchema)})
  const navigate = useNavigate()

  const onSubmit = async (data) => {
    try {
      await signUpUser(data)
      toast.success("Conta criada. Entre para continuar.")
      navigate("/signin")
    } catch (error) {
      // 400 sem erros de campo = e-mail ou nome de usuário já cadastrado
      if (error.status === 400 && error.errors?.length === 0) toast.error("Já existe uma conta com esse e-mail ou nome de usuário.")
      else if (error.status === 400) toast.error("Confira os dados informados.")
      else toast.error("Não foi possível criar a conta agora. Tente novamente.")
    }
  }

  return (
    <AuthCard title="Criar conta" subtitle="Leva menos de um minuto.">
      <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <FormField id="username" label="Nome de usuário" autoComplete="username" registration={register("username")} error={errors.username?.message} />
        <FormField id="email" label="E-mail" type="email" autoComplete="email" registration={register("email")} error={errors.email?.message} />
        <FormField id="password" label="Senha" type="password" autoComplete="new-password" registration={register("password")} error={errors.password?.message} />
        <FormField id="confirmPassword" label="Confirme a senha" type="password" autoComplete="new-password" registration={register("confirmPassword")} error={errors.confirmPassword?.message} />
        <Button className="mt-2 w-full" size="lg" type="submit" disabled={isSubmitting}>Criar conta</Button>
        <p className="mt-3 text-center text-sm text-muted-foreground">
          Já tem conta? <Link to="/signin" className="font-medium text-primary underline-offset-4 hover:underline">Entrar</Link>
        </p>
      </form>
    </AuthCard>
  )
}
