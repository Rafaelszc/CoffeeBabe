import { api, query } from "./apiClient"

// Sem preços no corpo: o servidor calcula tudo (RF-ORD-4).
export const placeOrder = ({ fulfillment, address }) =>
  api("/api/orders", { method: "POST", body: { fulfillment, address } })

export const fetchMyOrders = ({ page = 0, size = 10 } = {}) => api(`/api/orders?${query({ page, size })}`)

export const cancelOrder = (id) => api(`/api/orders/${id}/cancel`, { method: "POST" })

export const fetchAllOrders = ({ page = 0, size = 10 } = {}) => api(`/api/orders/all?${query({ page, size })}`)

export const updateOrderStatus = (id, status) =>
  api(`/api/orders/${id}/status`, { method: "PATCH", body: { status } })
