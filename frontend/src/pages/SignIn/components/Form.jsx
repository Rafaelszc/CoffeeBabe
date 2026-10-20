import { Button } from "@/components/ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { signInSchema } from "@/schemas/userSchema"
import { useContext } from "react"
import { Link, useNavigate } from "react-router-dom"
import { signInUser } from "@/api/userService"
import { AuthContext } from "@/context/AuthContext"
import { AuthCard } from "@/components/site/AuthCard"
import { FormField } from "@/components/site/FormField"


export const Form = () => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm({resolver: zodResolver(signInSchema)})
  const navigate = useNavigate()
  const { setHasUser } = useContext(AuthContext)

  const onSubmit = async (data) => {
    const status = await signInUser(data)
    if (status.ok) {
      setHasUser(true)
      navigate("/")
    }
      
    reset()
  }

  return (
    <AuthCard title="Entrar" subtitle="Acesse sua conta para fazer pedidos.">
      <form action={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <FormField id="email" label="E-mail" type="email" autoComplete="email" registration={register("email")} error={errors.email?.message} />
        <FormField id="password" label="Senha" type="password" autoComplete="current-password" registration={register("password")} error={errors.password?.message} />
        <Button className="mt-2 w-full" size="lg" type="submit">Entrar</Button>
        <p className="mt-3 text-center text-sm text-muted-foreground">
          Ainda não tem conta? <Link to="/signup" className="font-medium text-primary underline-offset-4 hover:underline">Criar conta</Link>
        </p>
      </form>
    </AuthCard>
  )
}
