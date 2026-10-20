import { fetchProducts } from "@/api/productService";
import { ProductCard } from "@/components/site/ProductCard";
import { Container } from "@/components/site/Container";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { CATEGORIES } from "@/lib/categories";
import { cn } from "@/lib/utils";
import { Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { motion } from "framer-motion";

const MotionDiv = motion.div

const normalize = (text = "") => text.normalize("NFD").replace(/\p{Diacritic}/gu, "").toLowerCase()

export const Products = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams, setSearchParams] = useSearchParams();
  const query = searchParams.get("q") ?? "";
  const category = searchParams.get("category") ?? "";

  useEffect(() => {
    const fetchData = async () => {
      const res = await fetchProducts()

      if (res.ok) setProducts(res.data)
      else console.error('Error fetching products')
      setLoading(false)
    };
    fetchData();
  }, []);

  const updateParam = (key, value) => {
    const next = new URLSearchParams(searchParams)
    if (value) next.set(key, value)
    else next.delete(key)
    setSearchParams(next, { replace: true })
  }

  // Filtro local até a busca da API existir (T6.3).
  const visible = useMemo(() => products.filter((p) => {
    const text = normalize(`${p.name} ${p.description ?? p.image?.alt ?? ""}`)
    return (!category || p.category === category) && text.includes(normalize(query))
  }), [products, query, category])

  const chips = [{ value: "", label: "Todos" }, ...CATEGORIES]

  return (
    <Container className="py-12 md:py-16">
      <div className="flex flex-col gap-3">
        <h1 className="text-4xl font-semibold md:text-5xl">Cardápio</h1>
        <p className="max-w-[60ch] text-lg leading-relaxed text-muted-foreground">
          Cafés quentes, bebidas geladas e petiscos para acompanhar.
        </p>
      </div>

      <div className="mt-10 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div role="group" aria-label="Categorias" className="-mx-4 flex gap-2 overflow-x-auto px-4 pb-1 md:mx-0 md:px-0">
          {chips.map((chip) => (
            <button
              key={chip.value || "all"}
              type="button"
              aria-pressed={category === chip.value}
              onClick={() => updateParam("category", chip.value)}
              className={cn(
                "h-10 shrink-0 cursor-pointer rounded-full border px-4 text-sm transition-colors",
                category === chip.value
                  ? "border-primary bg-primary text-primary-foreground"
                  : "border-border bg-background text-foreground hover:bg-secondary"
              )}
            >
              {chip.label}
            </button>
          ))}
        </div>
        <div className="relative md:w-72">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <label htmlFor="products-search" className="sr-only">Buscar no cardápio</label>
          <Input
            id="products-search"
            type="search"
            value={query}
            onChange={(e) => updateParam("q", e.target.value)}
            placeholder="Buscar pelo nome"
            className="rounded-full pl-10"
          />
        </div>
      </div>

      <div className="mt-10">
        {loading ? (
          <div className="grid grid-cols-1 gap-x-6 gap-y-12 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => <ProductCard key={i} isLoading />)}
          </div>
        ) : visible.length === 0 ? (
          <div className="flex flex-col items-center gap-4 rounded-2xl bg-secondary px-6 py-16 text-center">
            <p className="text-lg font-medium">Nenhum produto encontrado</p>
            <p className="text-muted-foreground">Tente outro nome ou outra categoria.</p>
            <Button variant="outline" onClick={() => setSearchParams({}, { replace: true })}>
              Limpar filtros
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-x-6 gap-y-12 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {visible.map((p, i) => (
              <MotionDiv
                key={p.id}
                initial={{ opacity: 0, y: 16 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: (i % 4) * 0.05, ease: [0.16, 1, 0.3, 1] }}
                viewport={{ once: true, amount: 0.2 }}
              >
                <ProductCard {...p} />
              </MotionDiv>
            ))}
          </div>
        )}
      </div>
    </Container>
  )
}
