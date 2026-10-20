import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious } from "@/components/ui/carousel"
import { ArrowRight } from "lucide-react"
import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import Autoplay from "embla-carousel-autoplay"
import { ProductCard } from "@/components/site/ProductCard"
import { Container } from "@/components/site/Container"
import { fetchBestSellers } from "@/api/productService"
import { Reveal } from "./Reveal"

export const BestSellers = () => {
  const [autoplay] = useState(() => Autoplay({ delay: 6000, stopOnInteraction: true, stopOnMouseEnter: true }))
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const data = await fetchBestSellers()
        setProducts(Array.isArray(data?.data) ? data.data : [])
      } catch {
        setFailed(true)
      } finally {
        setLoading(false)
      }
    }

    fetchProducts()
  }, [])

  return (
    <section className="py-16 md:py-24">
      <Container>
        <Reveal className="mb-10 flex items-end justify-between gap-6">
          <h2 className="text-3xl font-semibold md:text-4xl">Favoritos da casa</h2>
          <Link className="group flex shrink-0 items-center gap-1.5 text-sm font-medium text-primary" to="/products">
            Ver cardápio
            <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
          </Link>
        </Reveal>

        {failed || (!loading && products.length === 0) ? (
          <p className="rounded-2xl bg-secondary px-6 py-10 text-center text-muted-foreground">
            Não foi possível carregar os favoritos agora. Confira o cardápio completo.
          </p>
        ) : (
          <Reveal>
            <Carousel opts={{ align: "start", loop: true }} plugins={[autoplay]} className="w-full">
              <CarouselContent className="-ml-6">
                {(loading ? Array.from({ length: 3 }) : products).map((p, i) => (
                  <CarouselItem key={p?.id ?? i} className="pl-6 sm:basis-1/2 lg:basis-1/3">
                    {loading ? <ProductCard isLoading /> : <ProductCard {...p} />}
                  </CarouselItem>
                ))}
              </CarouselContent>
              <CarouselPrevious className="-left-5 hidden lg:flex" />
              <CarouselNext className="-right-5 hidden lg:flex" />
            </Carousel>
          </Reveal>
        )}
      </Container>
    </section>
  )
}
