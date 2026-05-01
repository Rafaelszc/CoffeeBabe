import { Carousel, CarouselContent, CarouselItem, CarouselNext, CarouselPrevious} from "@/components/ui/carousel"
import { ArrowRight } from "lucide-react"
import { useRef } from "react"
import { Link } from "react-router-dom"
import Autoplay from "embla-carousel-autoplay"
import { ProductCard } from "@/components/site/ProductCard"
import { motion } from "framer-motion"

const bestSellers = [
  {
    id: 1,
    name: "Cappuccino",
    image: {
      url: "src/assets/product-cappuccino.jpg",
      alt: "A creamy espresso-based drink with steamed milk.",
    },
    price: 4.00
  },
  {
    id: 2,
    name: "Mocha",
    image: {
      url: "src/assets/product-mocha.jpg",
      alt: "A rich, chocolate-flavored coffee drink.",
    },
    price: 4.50
  },
  {
    id: 3,
    name: "Cake",
    image: {
      url: "src/assets/product-cake.jpg",
      alt: "A delicious, moist cake perfect for any occasion.",
    },
    price: 3.75
  },
  {
    id: 4,
    name: "Croissant",
    image: {
      url: "src/assets/product-croissant.jpg",
      alt: "A flaky, buttery pastry perfect for breakfast.",
    },
    price: 2.50
  },
]

export const BestSellers = () => {
  const autoplay = useRef(Autoplay({ delay: 4500, stopOnInteraction: false }));

  return (
    <section className="px-20 py-10 bg-white/40">
      <div className="flex flex-col gap-3">
        <span className="text-secondary font-light tracking-widest ">Featured</span>
        <div className="flex w-full items-center justify-between">
          <h1 className=" text-4xl">House Favorites</h1>
          <div>
            <Link className="flex items-center gap-2" to="/products">
              <span className="text-sm text-secondary">
                See more
              </span>
              <ArrowRight className="text-secondary" size={15} />
            </Link>
          </div>
        </div>
      </div>
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        whileInView={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.7 }}
        className="mt-10"
      >
        <Carousel
            opts={{ align: "start", loop: true }}
            plugins={[autoplay.current]}
            className="w-full"
          >
            <CarouselContent className="mx-4">
              {bestSellers.map((p) => (
                <CarouselItem key={p.id} className="px-4 sm:basis-1/2 lg:basis-1/3">
                  <ProductCard {...p} />
                </CarouselItem>
              ))}
            </CarouselContent>
            <CarouselPrevious className="hidden md:flex -left-4" />
            <CarouselNext className="hidden md:flex -right-4" />
          </Carousel>
      </motion.div>
    </section>
  )
}