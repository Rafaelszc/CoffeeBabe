import z from "zod";

// Mesmas regras do backend (SignUpRequestDTO): senha de 8 a 72 caracteres.
const password = z.string("Informe a senha")
  .min(8, "A senha deve ter no mínimo 8 caracteres")
  .max(72, "A senha deve ter no máximo 72 caracteres")

export const signInSchema = z.object({
  email: z.email("E-mail inválido"),
  password: z.string("Informe a senha").min(1, "Informe a senha")
})

export const signUpSchema = z.object({
  username: z.string("Informe o nome de usuário").min(3, "O nome de usuário deve ter no mínimo 3 caracteres").max(30, "O nome de usuário deve ter no máximo 30 caracteres"),
  email: z.email("E-mail inválido"),
  password,
  confirmPassword: z.string("Confirme a senha")
}).refine((data) => data.password === data.confirmPassword, {
  error: "As senhas não coincidem",
  path: ["confirmPassword"]
})
