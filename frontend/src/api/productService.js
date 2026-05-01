import { cartData, productsData } from "@/data/mockData"
import { toast } from "sonner"

export const fetchProducts = async () => {
    // mock data
    return {ok: true, data: productsData}
}

export const fetchCart = async () => {
  // mock data
  return {ok: true, data: cartData}
}

export const addToCart = async (product) => {
  toast.success("Add " + product.name + " from cart")
}

export const removeFromCart = async (product) => {
  toast.error("Remove " + product.name + " from cart")
}