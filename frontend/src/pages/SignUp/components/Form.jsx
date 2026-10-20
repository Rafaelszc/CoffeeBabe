import { Button } from "@/components/ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { signUpSchema } from "@/schemas/userSchema"
import { Link, useNavigate } from "react-router-dom"
import { signUpUser } from "@/api/userService"
import { AuthCard } from "@/components/site/AuthCard"
import { FormField } from "@/components/site/FormField"


export const Form = () => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm({resolver: zodResolver(signUpSchema)})
  const navigate = useNavigate()

  const onSubmit = async (data) => {
    const status = await signUpUser(data)
    if (status.ok) navigate("/signin")
    
    reset()
  }

  return (
    <AuthCard title="Criar conta" subtitle="Leva menos de um minuto.">
      <form action={handleSubmit(onSubmit)} className="flex flex-col gap-3">
        <FormField id="username" label="Nome de usuário" autoComplete="username" registration={register("username")} error={errors.username?.message} />
        <FormField id="email" label="E-mail" type="email" autoComplete="email" registration={register("email")} error={errors.email?.message} />
        <FormField id="password" label="Senha" type="password" autoComplete="new-password" registration={register("password")} error={errors.password?.message} />
        <FormField id="confirmPassword" label="Confirme a senha" type="password" autoComplete="new-password" registration={register("confirmPassword")} error={errors.confirmPassword?.message} />
        <Button className="mt-2 w-full" size="lg" type="submit">Criar conta</Button>
        <p className="mt-3 text-center text-sm text-muted-foreground">
          Já tem conta? <Link to="/signin" className="font-medium text-primary underline-offset-4 hover:underline">Entrar</Link>
        </p>
      </form>
    </AuthCard>
  )
}
