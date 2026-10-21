import { cancelOrder, fetchMyOrders } from "@/api/orderService"
import { Container } from "@/components/site/Container"
import { OrderCard } from "@/components/site/OrderCard"
import { Pager } from "@/components/site/Pager"
import { Button } from "@/components/ui/button"
import { canCancel } from "@/lib/orders"
import { usePaged } from "@/lib/usePaged"
import { Receipt } from "lucide-react"
import { Link } from "react-router-dom"
import { toast } from "sonner"

const fetchPage = (page) => fetchMyOrders({ page })

export const Orders = () => {
  const { page, setPage, items, totalPages, loading, failed, reload } = usePaged(fetchPage)

  const handleCancel = async (order) => {
    if (!window.confirm("Cancelar este pedido?")) return
    try {
      await cancelOrder(order.id)
      toast.success("Pedido cancelado.")
    } catch (error) {
      toast.error(error.status === 409 ? "Este pedido já está em preparo e não pode mais ser cancelado." : "Não foi possível cancelar agora.")
    }
    reload()
  }

  return (
    <Container className="max-w-3xl py-12 md:py-16">
      <h1 className="text-4xl font-semibold md:text-5xl">Meus pedidos</h1>

      <div className="mt-10 flex flex-col gap-4">
        {loading ? (
          Array.from({ length: 2 }).map((_, i) => <div key={i} className="h-48 animate-pulse rounded-2xl bg-secondary" />)
        ) : failed ? (
          <p className="rounded-2xl bg-secondary px-6 py-12 text-center text-muted-foreground">
            Não foi possível carregar seus pedidos agora. Tente novamente em instantes.
          </p>
        ) : items.length === 0 ? (
          <div className="flex flex-col items-center gap-4 rounded-2xl bg-secondary px-6 py-16 text-center">
            <Receipt className="size-8 text-primary" strokeWidth={1.5} />
            <p className="text-lg font-medium">Você ainda não fez pedidos</p>
            <Button asChild>
              <Link to="/products">Ver cardápio</Link>
            </Button>
          </div>
        ) : (
          items.map((order) => (
            <OrderCard key={order.id} order={order}>
              {canCancel(order.status) && (
                <Button variant="outline" onClick={() => handleCancel(order)}>Cancelar pedido</Button>
              )}
            </OrderCard>
          ))
        )}
      </div>

      <Pager page={page} totalPages={totalPages} onChange={setPage} />
    </Container>
  )
}
