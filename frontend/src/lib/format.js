const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" })

export const formatPrice = (value) => brl.format(Number(value) || 0)

// Caminhos relativos do seed ("src/assets/...") precisam partir da raiz do site.
export const resolveImage = (url) => {
  if (!url) return undefined
  // product images are served by the backend (GET /api/product/{id}/image)
  if (url.startsWith("/api/")) return `${import.meta.env.VITE_API_URL ?? "http://localhost:8080"}${url}`
  return /^(https?:|data:|\/)/.test(url) ? url : `/${url}`
}
