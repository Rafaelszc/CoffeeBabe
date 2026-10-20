import { cartData, productsData } from "@/data/mockData"
import { toast } from "sonner"

const BASE_URL = import.meta.env.BASE_URL || "http://localhost:8080"

export const fetchProducts = async () => {
    // mock data
    return {ok: true, data: productsData}
}

export const fetchCart = async () => {
  const res = await fetch("http://localhost:8080/api/cart",
    {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    })

  const data = await res.json()

  return data
}

export const fetchBestSellers = async () => {
  const res = await fetch(`${BASE_URL}/api/product/get/best`,
    {
      method: "GET",
      headers: { "Content-Type": "application/json" }
    }
  )
  const data = await res.json()

  return data
}

export const addToCart = async (product) => {
  toast.success(`${product.name} adicionado ao carrinho`)
}

export const removeFromCart = async (product) => {
  toast.success(`${product.name} removido do carrinho`)
}