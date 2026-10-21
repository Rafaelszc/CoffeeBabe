// Valor exibido antes de fechar o pedido; o total oficial vem do servidor.
export const DELIVERY_FEE = 20

export const STATUS_LABELS = {
  PENDING: "Recebido",
  CONFIRMED: "Confirmado",
  READY: "Pronto",
  OUT_FOR_DELIVERY: "Saiu para entrega",
  COMPLETED: "Concluído",
  CANCELLED: "Cancelado",
}

export const FULFILLMENT_LABELS = {
  PICKUP: "Retirada na loja",
  DELIVERY: "Entrega",
}

export const canCancel = (status) => status === "PENDING" || status === "CONFIRMED"

// Espelha OrderStatus.canMoveTo do backend.
export const nextStatuses = (status, fulfillment) => ({
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["READY", "CANCELLED"],
  READY: [fulfillment === "DELIVERY" ? "OUT_FOR_DELIVERY" : "COMPLETED"],
  OUT_FOR_DELIVERY: ["COMPLETED"],
}[status] ?? [])

const dateTime = new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" })

export const formatDate = (iso) => dateTime.format(new Date(iso))
