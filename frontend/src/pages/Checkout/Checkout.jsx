import { addToCart, decreaseCartItem, fetchCart, removeCartItem } from "@/api/cartService";
import { placeOrder } from "@/api/orderService";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ShoppingBag } from "lucide-react";
import { toast } from "sonner";
import { ProductCartCard } from "./components/ProductCartCard";
import { Container } from "@/components/site/Container";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { formatPrice } from "@/lib/format";
import { DELIVERY_FEE, FULFILLMENT_LABELS } from "@/lib/orders";
import { cn } from "@/lib/utils";

export const Checkout = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState(false);
  const [busy, setBusy] = useState(false);
  const [fulfillment, setFulfillment] = useState("PICKUP");
  const [address, setAddress] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchCart()
      .then(setCart)
      .catch(() => setFailed(true))
      .finally(() => setLoading(false))
  }, []);

  // Toda mudança vai ao servidor e recarrega o carrinho, que traz os totais calculados lá.
  const updateCart = async (action) => {
    setBusy(true)
    try {
      await action()
      setCart(await fetchCart())
    } catch {
      toast.error("Não foi possível atualizar o carrinho agora.")
    } finally {
      setBusy(false)
    }
  }

  const handlePlaceOrder = async () => {
    const delivery = fulfillment === "DELIVERY"
    if (delivery && !address.trim()) {
      toast.error("Informe o endereço de entrega.")
      return
    }

    setBusy(true)
    try {
      await placeOrder({ fulfillment, address: delivery ? address.trim() : undefined })
      toast.success("Pedido enviado! Acompanhe o status em Meus pedidos.")
      navigate("/orders")
    } catch (error) {
      toast.error(error.status === 400 ? "Confira o carrinho e o endereço." : "Não foi possível finalizar o pedido agora.")
      setBusy(false)
    }
  }

  const items = cart?.products ?? []
  const deliveryFee = fulfillment === "DELIVERY" ? DELIVERY_FEE : 0

  return (
    <Container className="py-12 md:py-16">
      <h1 className="text-4xl font-semibold md:text-5xl">Carrinho</h1>

      <div className="mt-10 grid grid-cols-1 gap-8 lg:grid-cols-3 lg:gap-12">
        <div className="flex flex-col gap-4 lg:col-span-2">
          {loading ? (
            Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-28 animate-pulse rounded-2xl bg-secondary" />
            ))
          ) : failed ? (
            <p className="rounded-2xl bg-secondary px-6 py-12 text-center text-muted-foreground">
              Não foi possível carregar o carrinho agora. Tente novamente em instantes.
            </p>
          ) : items.length === 0 ? (
            <div className="flex flex-col items-center gap-4 rounded-2xl bg-secondary px-6 py-16 text-center">
              <ShoppingBag className="size-8 text-primary" strokeWidth={1.5} />
              <p className="text-lg font-medium">Seu carrinho está vazio</p>
              <Button asChild>
                <Link to="/products">Ver cardápio</Link>
              </Button>
            </div>
          ) : (
            items.map((item) => (
              <ProductCartCard
                key={item.product.id}
                {...item}
                disabled={busy}
                onIncrease={() => updateCart(() => addToCart(item.product.id))}
                onDecrease={() => updateCart(() => decreaseCartItem(item.product.id))}
                onRemove={() => updateCart(() => removeCartItem(item.product.id))}
              />
            ))
          )}
        </div>

        <aside className="h-fit rounded-2xl border bg-card p-6 lg:sticky lg:top-24">
          <h2 className="text-xl font-semibold">Resumo do pedido</h2>

          <fieldset className="mt-6 flex flex-col gap-2">
            <legend className="mb-2 text-sm font-medium">Como você quer receber?</legend>
            {Object.entries(FULFILLMENT_LABELS).map(([value, label]) => (
              <label
                key={value}
                className={cn(
                  "flex cursor-pointer items-center justify-between rounded-xl border px-4 py-3 text-sm transition-colors",
                  fulfillment === value ? "border-primary bg-primary/5" : "hover:bg-secondary"
                )}
              >
                <span className="flex items-center gap-3">
                  <input
                    type="radio"
                    name="fulfillment"
                    value={value}
                    checked={fulfillment === value}
                    onChange={() => setFulfillment(value)}
                    className="accent-primary"
                  />
                  {label}
                </span>
                <span className="tabular-nums text-muted-foreground">
                  {value === "DELIVERY" ? formatPrice(DELIVERY_FEE) : "Grátis"}
                </span>
              </label>
            ))}
          </fieldset>

          {fulfillment === "DELIVERY" && (
            <div className="mt-4 flex flex-col gap-2">
              <Label htmlFor="address">Endereço de entrega</Label>
              <textarea
                id="address"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                maxLength={300}
                rows={3}
                autoComplete="street-address"
                className="w-full rounded-xl border border-input bg-background px-3.5 py-2.5 text-base outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 dark:bg-input/30"
              />
            </div>
          )}

          <dl className="mt-6 flex flex-col gap-3 text-sm">
            <div className="flex justify-between">
              <dt className="text-muted-foreground">Subtotal</dt>
              <dd className="tabular-nums">{formatPrice(cart?.subtotal)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-muted-foreground">Impostos (3%)</dt>
              <dd className="tabular-nums">{formatPrice(cart?.tax)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-muted-foreground">Entrega</dt>
              <dd className="tabular-nums">{formatPrice(deliveryFee)}</dd>
            </div>
            <div className="mt-2 flex justify-between border-t pt-4 text-base font-semibold">
              <dt>Total</dt>
              <dd className="tabular-nums">{formatPrice(Number(cart?.total ?? 0) + deliveryFee)}</dd>
            </div>
          </dl>

          <Button className="mt-6 w-full" size="lg" disabled={busy || items.length === 0} onClick={handlePlaceOrder}>
            Finalizar pedido
          </Button>
          <p className="mt-4 text-xs leading-relaxed text-muted-foreground">
            O pagamento é feito na retirada ou na entrega.
          </p>
        </aside>
      </div>
    </Container>
  )
}
