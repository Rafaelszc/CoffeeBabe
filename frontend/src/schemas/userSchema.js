import z from "zod";

export const signInSchema = z.object({
  email: z.email("E-mail inválido"),
  password: z.string("Informe a senha").min(6, "A senha deve ter no mínimo 6 caracteres").max(30, "A senha deve ter no máximo 30 caracteres")
})

export const signUpSchema = z.object({
  username: z.string("Informe o nome de usuário").min(3, "O nome de usuário deve ter no mínimo 3 caracteres").max(30, "O nome de usuário deve ter no máximo 30 caracteres"),
  email: z.email("E-mail inválido"),
  password: z.string("Informe a senha").min(6, "A senha deve ter no mínimo 6 caracteres").max(30, "A senha deve ter no máximo 30 caracteres"),
  confirmPassword: z.string("Confirm Informe a senha").min(6, "Confirm A senha deve ter no mínimo 6 caracteres").max(30, "Confirm A senha deve ter no máximo 30 caracteres")
}).refine((data) => data.password === data.confirmPassword, {
  error: "As senhas não coincidem",
  path: ["confirmPassword"]
})