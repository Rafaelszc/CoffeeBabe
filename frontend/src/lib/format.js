const brl = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" })

export const formatPrice = (value) => brl.format(Number(value) || 0)

// Caminhos relativos do seed ("src/assets/...") precisam partir da raiz do site.
export const resolveImage = (url) => {
  if (!url) return undefined
  return /^(https?:|data:|\/)/.test(url) ? url : `/${url}`
}
