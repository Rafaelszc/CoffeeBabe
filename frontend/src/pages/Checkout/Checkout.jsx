import { fetchCart } from "@/api/productService";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ShoppingBag } from "lucide-react";
import { ProductCartCard } from "./components/ProductCartCard";
import { Container } from "@/components/site/Container";
import { Button } from "@/components/ui/button";
import { formatPrice } from "@/lib/format";

export const Checkout = () => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    const getCart = async () => {
      try {
        const res = await fetchCart()

        if (res.ok) setCart(res.data)
        else setFailed(true)
      } catch {
        setFailed(true)
      } finally {
        setLoading(false)
      }
    };
    getCart();
  }, []);

  const items = cart?.products ?? []
  const subtotal = cart?.subtotal ?? 0
  const tax = cart?.tax ?? 0

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
            items.map((item) => <ProductCartCard key={item.id} {...item} />)
          )}
        </div>

        <aside className="h-fit rounded-2xl border bg-card p-6 lg:sticky lg:top-24">
          <h2 className="text-xl font-semibold">Resumo do pedido</h2>
          <dl className="mt-6 flex flex-col gap-3 text-sm">
            <div className="flex justify-between">
              <dt className="text-muted-foreground">Subtotal</dt>
              <dd className="tabular-nums">{formatPrice(subtotal)}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-muted-foreground">Impostos</dt>
              <dd className="tabular-nums">{formatPrice(tax)}</dd>
            </div>
            <div className="mt-2 flex justify-between border-t pt-4 text-base font-semibold">
              <dt>Total</dt>
              <dd className="tabular-nums">{formatPrice(subtotal + tax)}</dd>
            </div>
          </dl>
          <p className="mt-4 text-xs leading-relaxed text-muted-foreground">
            A taxa de entrega de R$ 20,00 é somada ao escolher entrega. Na retirada não há taxa.
          </p>
        </aside>
      </div>
    </Container>
  )
}
