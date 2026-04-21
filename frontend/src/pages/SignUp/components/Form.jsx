import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Button } from "@/components/ui/button"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { signUpSchema } from "@/schemas/userSchema"
import { Coffee, Eye, EyeClosed } from "lucide-react"
import { useState } from "react"
import { Link } from "react-router-dom"


export const Form = () => {
  const { register, handleSubmit, reset, formState: { errors } } = useForm({resolver: zodResolver(signUpSchema)})
  const [seePassword, setSeePassword] = useState(false)
  const [seeConfirmPassword, setSeeConfirmPassword] = useState(false)

  const onSubmit = (data) => {
    alert(JSON.stringify(data))
    reset()
  }

  return (
    <section className="flex items-center justify-center py-16 bg-white/40">
      <div className="w-full max-w-md bg-white p-10 rounded-4xl shadow-sm">
        <div className="flex flex-col items-center gap-2 mb-10">
          <Coffee className="text-secondary" />
          <h2 className="text-3xl text-secondary-foreground font-semibold" >Sign Up</h2>
          <p className="text-muted-foreground" >Create your account</p>
        </div>

        <div>
          <form action={handleSubmit(onSubmit)} className="flex flex-col gap-5">
            <div className="flex flex-col gap-3">
              <Label htmlFor="username">Username</Label>
              <Input id="username" type="text" {...register("username")} className="bg-secondary/10 py-5" />
              <p className="text-red-500 text-sm min-h-5">
                {errors.username?.message}
              </p>
            </div>

            <div className="flex flex-col gap-3">
              <Label htmlFor="email">E-mail</Label>
              <Input id="email" type="email" {...register("email")} className="bg-secondary/10 py-5" />
              <p className="text-red-500 text-sm min-h-5">
                {errors.email?.message}
              </p>
            </div>

            <div className="flex flex-col gap-3">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                {seePassword 
                ? <Eye onClick={() => setSeePassword(!seePassword)} className="absolute right-3 top-1/2 -translate-y-1/2 h-4 cursor-pointer"/> 
                : <EyeClosed onClick={() => setSeePassword(!seePassword)} className="absolute right-3 top-1/2 -translate-y-1/2 h-4 cursor-pointer"/>}

                <Input id="password" type={seePassword ? "text" : "password"} {...register("password")} className="bg-secondary/10 py-5" />
              </div>
              <p className="text-red-500 text-sm min-h-5">
                {errors.password?.message}
              </p>
            </div>

            <div className="flex flex-col gap-3">
              <Label htmlFor="confirmPassword">Confirm your password</Label>
              <div className="relative">
                {seeConfirmPassword 
                ? <Eye onClick={() => setSeeConfirmPassword(!seeConfirmPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 h-4 cursor-pointer"/> 
                : <EyeClosed onClick={() => setSeeConfirmPassword(!seeConfirmPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 h-4 cursor-pointer"/>}

                <Input id="confirmPassword" type={seeConfirmPassword ? "text" : "password"} {...register("confirmPassword")} className="bg-secondary/10 py-5" />
              </div>
              <p className="text-red-500 text-sm min-h-5">
                {errors.confirmPassword?.message}
              </p>
            </div>

            <div className="flex flex-col items-center gap-4">
              <Button className="cursor-pointer bg-secondary w-full rounded-full py-5 hover:bg-secondary/80 transition-colors duration-300" type="submit">Create account</Button>
              <span className="text-sm text-secondary">Already have an account? <Link to="/signin" className="text-blue-700 hover:text-blue-400 transition-colors duration-300">Sign In</Link></span>
            </div>
          </form>
        </div>
      </div>
    </section>
  )
}