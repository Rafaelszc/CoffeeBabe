import { beforeEach, describe, expect, it, vi } from "vitest"
import { render, screen } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import { MemoryRouter } from "react-router-dom"
import { toast } from "sonner"
import { decreaseCartItem, fetchCart } from "@/api/cartService"
import { placeOrder } from "@/api/orderService"
import { Checkout } from "./Checkout"

vi.mock("@/api/cartService", () => ({
  fetchCart: vi.fn(),
  addToCart: vi.fn(),
  decreaseCartItem: vi.fn(),
  removeCartItem: vi.fn(),
}))
vi.mock("@/api/orderService", () => ({ placeOrder: vi.fn() }))
vi.mock("sonner", () => ({ toast: { error: vi.fn(), success: vi.fn() } }))

const cart = {
  products: [{
    product: { id: "p1", name: "Latte", description: "cremoso", price: 4, category: "HOT_COFFEE", imageUrl: "/api/product/p1/image" },
    quantity: 2,
    lineTotal: 8,
  }],
  subtotal: 8,
  tax: 0.24,
  total: 8.24,
}

const renderCheckout = async () => {
  render(<MemoryRouter><Checkout /></MemoryRouter>)
  await screen.findByText("Latte")
}

describe("Checkout", () => {
  beforeEach(() => {
    vi.clearAllMocks()
    fetchCart.mockResolvedValue(cart)
  })

  it("shows items with quantity and the server totals", async () => {
    await renderCheckout()

    expect(screen.getByLabelText("Quantidade de Latte").textContent).toBe("2")
    expect(screen.getByText(/8,24/)).toBeTruthy()
  })

  it("adds the delivery fee to the total when delivery is chosen", async () => {
    await renderCheckout()

    await userEvent.setup().click(screen.getByRole("radio", { name: /Entrega/ }))

    expect(screen.getByText(/28,24/)).toBeTruthy()
    expect(screen.getByLabelText("Endereço de entrega")).toBeTruthy()
  })

  it("decreasing goes to the API and reloads the cart", async () => {
    decreaseCartItem.mockResolvedValue(null)
    await renderCheckout()

    await userEvent.setup().click(screen.getByLabelText("Diminuir Latte"))

    expect(decreaseCartItem).toHaveBeenCalledWith("p1")
    expect(fetchCart).toHaveBeenCalledTimes(2)
  })

  it("requires an address for delivery and sends no prices", async () => {
    placeOrder.mockResolvedValue({})
    await renderCheckout()
    const user = userEvent.setup()

    await user.click(screen.getByRole("radio", { name: /Entrega/ }))
    await user.click(screen.getByRole("button", { name: "Finalizar pedido" }))
    expect(toast.error).toHaveBeenCalledWith("Informe o endereço de entrega.")
    expect(placeOrder).not.toHaveBeenCalled()

    await user.type(screen.getByLabelText("Endereço de entrega"), "Rua A, 10")
    await user.click(screen.getByRole("button", { name: "Finalizar pedido" }))
    expect(placeOrder).toHaveBeenCalledWith({ fulfillment: "DELIVERY", address: "Rua A, 10" })
  })
})
