import { apiUrl } from "@/api/apiClient"

const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" })

export const formatPrice = (value) => brl.format(Number(value) || 0)

// Imagens de produto vêm do backend (GET /api/product/{id}/image); o resto é caminho do próprio site.
export const resolveImage = (url) => {
  if (!url) return undefined
  if (url.startsWith("/api/")) return apiUrl(url)
  return /^(https?:|data:|\/)/.test(url) ? url : `/${url}`
}
