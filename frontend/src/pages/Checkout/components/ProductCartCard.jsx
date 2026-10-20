import { removeFromCart } from "@/api/productService"
import { Button } from "@/components/ui/button"
import { formatPrice, resolveImage } from "@/lib/format"
import { Trash2 } from "lucide-react"

export const ProductCartCard = ({ id, name, image, price }) => {
    const handleRemoveFromCart = () => {
        removeFromCart({id, name, image, price})
    }
    return (
        <article className="flex items-center gap-4 rounded-2xl border bg-card p-3 pr-4 sm:gap-5">
            <img className="size-20 shrink-0 rounded-xl object-cover sm:size-24" src={resolveImage(image?.url)} alt={name} />
            <div className="min-w-0 flex-1">
                <h2 className="truncate text-lg font-medium">{name}</h2>
                <p className="line-clamp-1 text-sm text-muted-foreground">{image?.alt}</p>
                <p className="mt-1.5 font-medium tabular-nums">{formatPrice(price)}</p>
            </div>
            <Button onClick={handleRemoveFromCart} variant="ghost" size="icon" aria-label={`Remover ${name} do carrinho`} className="text-muted-foreground hover:text-destructive">
                <Trash2 />
            </Button>
        </article>
    )
}
