import z from "zod";

export const signInSchema = z.object({
  email: z.email("Invalid email address"),
  password: z.string("Password is required").min(6, "Password must be at least 6 characters").max(30, "Password must be at most 30 characters")
})

export const signUpSchema = z.object({
  username: z.string("Username is required").min(3, "Username must be at least 3 characters").max(30, "Username must be at most 30 characters"),
  email: z.email("Invalid email address"),
  password: z.string("Password is required").min(6, "Password must be at least 6 characters").max(30, "Password must be at most 30 characters"),
  confirmPassword: z.string("Confirm Password is required").min(6, "Confirm Password must be at least 6 characters").max(30, "Confirm Password must be at most 30 characters")
}).refine((data) => data.password === data.confirmPassword, {
  error: "Passwords don't match",
  path: ["confirmPassword"]
})