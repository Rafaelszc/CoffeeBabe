import { describe, expect, it, vi } from "vitest"
import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { MemoryRouter } from "react-router-dom"
import { toast } from "sonner"
import { AuthContext } from "@/context/auth"
import { ApiError } from "@/api/apiClient"
import { signInUser } from "@/api/userService"
import { Form } from "./Form"

vi.mock("@/api/userService", () => ({ signInUser: vi.fn() }))
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }))

const renderForm = () => {
  const refreshUser = vi.fn()
  render(
    <MemoryRouter>
      <AuthContext.Provider value={{ refreshUser }}>
        <Form />
      </AuthContext.Provider>
    </MemoryRouter>
  )
  return { refreshUser }
}

const fillAndSubmit = async (email, password) => {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText("E-mail"), email)
  await user.type(screen.getByLabelText("Senha"), password)
  await user.click(screen.getByRole("button", { name: "Entrar" }))
}

describe("SignIn form", () => {
  it("signs in and refreshes the session user", async () => {
    signInUser.mockResolvedValue(null)
    const { refreshUser } = renderForm()

    await fillAndSubmit("ana@mail.com", "senha-forte")

    expect(signInUser).toHaveBeenCalledWith({ email: "ana@mail.com", password: "senha-forte" })
    expect(refreshUser).toHaveBeenCalled()
  })

  it("shows a generic message on wrong credentials", async () => {
    signInUser.mockRejectedValue(new ApiError("Invalid email or password!", 401))
    const { refreshUser } = renderForm()

    await fillAndSubmit("ana@mail.com", "errada")

    expect(toast.error).toHaveBeenCalledWith("E-mail ou senha inválidos.")
    expect(refreshUser).not.toHaveBeenCalled()
  })

  it("tells the user to wait when sign-in is rate limited", async () => {
    signInUser.mockRejectedValue(new ApiError("Too many attempts, try again later!", 429))
    renderForm()

    await fillAndSubmit("ana@mail.com", "errada")

    expect(toast.error).toHaveBeenCalledWith("Muitas tentativas. Aguarde alguns minutos e tente de novo.")
  })

  it("validates the form before calling the API", async () => {
    signInUser.mockClear()
    renderForm()
    const user = userEvent.setup()

    await user.type(screen.getByLabelText("E-mail"), "ana@mail.com")
    await user.click(screen.getByRole("button", { name: "Entrar" }))

    expect(await screen.findByText("Informe a senha")).toBeTruthy()
    expect(signInUser).not.toHaveBeenCalled()
  })
})
