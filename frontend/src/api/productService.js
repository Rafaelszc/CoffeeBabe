import { api, query } from "./apiClient"

export const fetchProducts = ({ q, category, page = 0, size = 12 } = {}) =>
  api(`/api/product?${query({ q, category, page, size })}`)

export const fetchBestSellers = () => api("/api/product/get/best")

export const createProduct = (product) => api("/api/product", { method: "POST", body: product })

export const updateProduct = (id, product) => api(`/api/product/${id}`, { method: "PUT", body: product })

export const deleteProduct = (id) => api(`/api/product/${id}`, { method: "DELETE" })

export const uploadProductImage = (id, file) => {
  const form = new FormData()
  form.append("file", file)
  return api(`/api/product/${id}/image`, { method: "POST", form })
}
