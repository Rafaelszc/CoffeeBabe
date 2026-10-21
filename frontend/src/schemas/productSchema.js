import z from "zod";

// Mesmas regras do ProductRequestDTO; o preço aceita vírgula ("4,50") e no máximo 2 casas.
export const productSchema = z.object({
  name: z.string().trim().min(1, "Informe o nome").max(60, "O nome deve ter no máximo 60 caracteres"),
  description: z.string().trim().min(1, "Informe a descrição").max(1000, "A descrição deve ter no máximo 1000 caracteres"),
  price: z.string("Informe o preço")
    .trim()
    .regex(/^\d{1,8}([.,]\d{1,2})?$/, "Use um valor como 4,50")
    .transform((value) => Number(value.replace(",", ".")))
    .refine((value) => value > 0, "O preço deve ser maior que zero"),
  category: z.enum(["HOT_COFFEE", "COLD_COFFEE", "SNACK"], "Escolha a categoria"),
})
