import { Plus } from "lucide-react"
import { Button } from "../ui/button"
import { addToCart } from "@/api/productService"

export const ProductCard = ({ id, name, image, price }) => {
    const handleAddToCart = () => {
        const product = {id, name, image, price}
        addToCart(product)
    }

    return (
        <article style={{height: "500px", width: "350px"}} className="overflow-hidden flex flex-col gap-5 rounded-2xl bg-white shadow-md m-5 pb-5 hover:scale-105 hover:shadow-lg transition-all duration-500 group">
            <div className="overflow-hidden">
                <img className="rounded-t-2xl group-hover:scale-110 delay-75 transition-transform duration-500" src={image.url} alt={name} />
            </div>
            <div className="px-5 flex flex-col gap-2">
                <h3 className="text-xl">{name}</h3>
                <p className="text-sm text-secondary">{image.alt}</p>
            </div>
            <div className="flex items-center justify-between px-5">
                <h3 className="text-lg font-light text-secondary tracking-wide">$ {price.toFixed(2)}</h3>
                <Button onClick={handleAddToCart} className="cursor-pointer hover:bg-secondary/80 px-4 py-4 bg-secondary rounded-full"><Plus />Add to cart</Button>
            </div>
        </article>
    )
}