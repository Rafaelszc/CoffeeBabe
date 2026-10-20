import { ArrowUpRight } from "lucide-react"
import { Link } from "react-router-dom"
import { Container } from "@/components/site/Container"
import { CATEGORIES } from "@/lib/categories"
import { cn } from "@/lib/utils"
import { Reveal } from "./Reveal"
import hotImage from "@/assets/product-cappuccino.jpg"
import coldImage from "@/assets/product-iced-latte.jpg"
import snackImage from "@/assets/product-croissant.jpg"

const images = { HOT_COFFEE: hotImage, COLD_COFFEE: coldImage, SNACK: snackImage }
const descriptions = {
  HOT_COFFEE: "Espresso, cappuccino e mocha.",
  COLD_COFFEE: "Lattes gelados para dias quentes.",
  SNACK: "Croissants, bolos e brownies.",
}

export const Categories = () => {
  return (
    <section className="py-16 md:py-24">
      <Container>
        <Reveal>
          <h2 className="mb-10 text-3xl font-semibold md:text-4xl">Escolha pelo momento</h2>
        </Reveal>
        <div className="grid gap-5 md:grid-cols-2 md:grid-rows-2">
          {CATEGORIES.map((category, i) => (
            <Reveal key={category.value} delay={i * 0.06} className={cn(i === 0 && "md:row-span-2")}>
              <Link
                to={`/products?category=${category.value}`}
                className="group relative block h-full min-h-56 overflow-hidden rounded-2xl"
              >
                <img
                  src={images[category.value]}
                  alt=""
                  loading="lazy"
                  className="absolute inset-0 size-full object-cover transition-transform duration-700 ease-out group-hover:scale-[1.04]"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-[oklch(0.18_0.02_55/0.78)] via-[oklch(0.18_0.02_55/0.2)] to-transparent" />
                <div className="relative flex h-full min-h-56 items-end justify-between gap-4 p-6 text-white">
                  <div>
                    <h3 className="text-2xl font-semibold">{category.label}</h3>
                    <p className="mt-1 text-sm text-white/85">{descriptions[category.value]}</p>
                  </div>
                  <ArrowUpRight className="size-6 shrink-0 transition-transform group-hover:-translate-y-0.5 group-hover:translate-x-0.5" />
                </div>
              </Link>
            </Reveal>
          ))}
        </div>
      </Container>
    </section>
  )
}
