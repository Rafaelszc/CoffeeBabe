import { Plus } from "lucide-react"
import { Button } from "../ui/button"
import { addToCart } from "@/api/productService"
import { formatPrice, resolveImage } from "@/lib/format"

export const ProductCard = ({ id, name, imageUrl, image, description, price, isLoading }) => {
  if (isLoading) {
    return (
      <article aria-hidden="true" className="flex animate-pulse flex-col gap-4">
        <div className="aspect-[4/3] rounded-2xl bg-secondary" />
        <div className="h-5 w-2/3 rounded-full bg-secondary" />
        <div className="h-4 w-full rounded-full bg-secondary" />
        <div className="h-10 w-full rounded-full bg-secondary" />
      </article>
    )
  }

  // O mock usa image.{url,alt}; a API usa imageUrl/description.
  const src = resolveImage(imageUrl ?? image?.url)
  const text = description ?? image?.alt

  const handleAddToCart = () => {
    addToCart({ id, name, imageUrl: src, description: text, price })
  }

  return (
    <article className="group flex h-full flex-col gap-4">
      <div className="overflow-hidden rounded-2xl bg-secondary">
        <img
          className="aspect-[4/3] w-full object-cover transition-transform duration-500 ease-out group-hover:scale-[1.04]"
          src={src}
          alt={name}
          loading="lazy"
        />
      </div>
      <div className="flex flex-1 flex-col gap-1.5">
        <h3 className="text-lg font-medium">{name}</h3>
        <p className="line-clamp-2 text-sm leading-relaxed text-muted-foreground">{text}</p>
      </div>
      <div className="flex items-center justify-between gap-3">
        <span className="text-base font-medium tabular-nums">{formatPrice(price)}</span>
        <Button onClick={handleAddToCart} variant="outline" aria-label={`Adicionar ${name} ao carrinho`}>
          <Plus /> Adicionar
        </Button>
      </div>
    </article>
  )
}
