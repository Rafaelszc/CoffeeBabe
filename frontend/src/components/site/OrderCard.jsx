import { formatPrice } from "@/lib/format"
import { FULFILLMENT_LABELS, STATUS_LABELS, formatDate } from "@/lib/orders"
import { cn } from "@/lib/utils"

// Resumo de um pedido; `children` recebe as ações (cancelar, mudar status).
export const OrderCard = ({ order, children }) => (
  <article className="rounded-2xl border bg-card p-5 sm:p-6">
    <header className="flex flex-wrap items-center justify-between gap-3">
      <div className="flex flex-col">
        <span className="text-sm text-muted-foreground">{formatDate(order.createdAt)}</span>
        <span className="font-medium">{FULFILLMENT_LABELS[order.fulfillment]}</span>
      </div>
      <span className={cn(
        "rounded-full px-3 py-1 text-sm font-medium",
        order.status === "CANCELLED" ? "bg-destructive/10 text-destructive" : "bg-primary/10 text-primary"
      )}>
        {STATUS_LABELS[order.status]}
      </span>
    </header>

    {order.address && <p className="mt-3 text-sm text-muted-foreground">{order.address}</p>}

    <ul className="mt-4 flex flex-col gap-1.5 text-sm">
      {order.items.map((item) => (
        <li key={item.productName} className="flex justify-between gap-4">
          <span>{item.quantity}× {item.productName}</span>
          <span className="tabular-nums">{formatPrice(item.lineTotal)}</span>
        </li>
      ))}
    </ul>

    <dl className="mt-4 flex flex-col gap-1 border-t pt-4 text-sm">
      <div className="flex justify-between text-muted-foreground">
        <dt>Impostos</dt><dd className="tabular-nums">{formatPrice(order.tax)}</dd>
      </div>
      {Number(order.deliveryFee) > 0 && (
        <div className="flex justify-between text-muted-foreground">
          <dt>Entrega</dt><dd className="tabular-nums">{formatPrice(order.deliveryFee)}</dd>
        </div>
      )}
      <div className="flex justify-between text-base font-semibold">
        <dt>Total</dt><dd className="tabular-nums">{formatPrice(order.total)}</dd>
      </div>
    </dl>

    {children && <div className="mt-5 flex flex-wrap items-center gap-3">{children}</div>}
  </article>
)
