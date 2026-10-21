import { api } from "./apiClient"

export const fetchCart = () => api("/api/cart/get")

export const addToCart = (productId) => api("/api/cart/add", { method: "POST", body: { productId } })

export const decreaseCartItem = (productId) =>
  api(`/api/cart/decrease?productId=${productId}`, { method: "PATCH" })

export const removeCartItem = (productId) =>
  api(`/api/cart/delete?productId=${productId}`, { method: "DELETE" })
