import { removeFromCart } from "@/api/productService"
import { Button } from "@/components/ui/button"
import { Trash2 } from "lucide-react"

export const ProductCartCard = ({ id, name, image, price }) => {
    const handleRemoveFromCart = () => {
        removeFromCart({id, name, image, price})
    }
    return (
        <article className="flex flex-row justify-between bg-white/80 w-full rounded-4xl px-5 py-5 shadow-sm">
            <div className="overflow-hidden flex gap-5">
                <img className="h-25 w-25 rounded-2xl" src={image.url}  alt={name} />
                <div>
                    <h2 className="text-xl">{name}</h2>
                    <p className="text-sm font-light text-secondary">{image.alt}</p>
                </div>
            </div>
            <div className="flex flex-col justify-between items-end">
                <Button onClick={handleRemoveFromCart} variant="destructive" className="cursor-pointer" >
                    <Trash2 className="text-secondary" />
                </Button>
                <div>
                    <h3 className="text-lg font-light text-secondary tracking-wide">$ {price.toFixed(2)}</h3>
                </div>
            </div>
        </article>
    )
}