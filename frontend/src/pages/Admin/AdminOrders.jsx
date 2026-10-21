import { fetchAllOrders, updateOrderStatus } from "@/api/orderService"
import { Container } from "@/components/site/Container"
import { OrderCard } from "@/components/site/OrderCard"
import { Pager } from "@/components/site/Pager"
import { Button } from "@/components/ui/button"
import { STATUS_LABELS, nextStatuses } from "@/lib/orders"
import { usePaged } from "@/lib/usePaged"
import { toast } from "sonner"

const fetchPage = (page) => fetchAllOrders({ page })

export const AdminOrders = () => {
  const { page, setPage, items, totalPages, loading, failed, reload } = usePaged(fetchPage)

  const handleStatus = async (order, status) => {
    try {
      await updateOrderStatus(order.id, status)
      toast.success(`Pedido marcado como ${STATUS_LABELS[status].toLowerCase()}.`)
    } catch (error) {
      toast.error(error.status === 409 ? "O pedido mudou de status. A lista foi atualizada." : "Não foi possível atualizar agora.")
    }
    reload()
  }

  return (
    <Container className="max-w-3xl py-12 md:py-16">
      <h1 className="text-4xl font-semibold md:text-5xl">Pedidos da loja</h1>

      <div className="mt-10 flex flex-col gap-4">
        {loading ? (
          Array.from({ length: 2 }).map((_, i) => <div key={i} className="h-48 animate-pulse rounded-2xl bg-secondary" />)
        ) : failed ? (
          <p className="rounded-2xl bg-secondary px-6 py-12 text-center text-muted-foreground">
            Não foi possível carregar os pedidos agora.
          </p>
        ) : items.length === 0 ? (
          <p className="rounded-2xl bg-secondary px-6 py-12 text-center text-muted-foreground">Nenhum pedido ainda.</p>
        ) : (
          items.map((order) => (
            <OrderCard key={order.id} order={order}>
              {nextStatuses(order.status, order.fulfillment).map((status) => (
                <Button
                  key={status}
                  variant={status === "CANCELLED" ? "outline" : "default"}
                  onClick={() => handleStatus(order, status)}
                >
                  {status === "CANCELLED" ? "Cancelar" : `Marcar como ${STATUS_LABELS[status].toLowerCase()}`}
                </Button>
              ))}
            </OrderCard>
          ))
        )}
      </div>

      <Pager page={page} totalPages={totalPages} onChange={setPage} />
    </Container>
  )
}
