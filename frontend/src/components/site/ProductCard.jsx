import { Plus } from "lucide-react"
import { Button } from "../ui/button"

export const ProductCard = ({ id, imagePath, title, description, price }) => {
    const handleAddToCart = () => {
        console.log(`Added product ${id} to cart`);
    }

    return (
        <article className="overflow-hidden flex flex-col gap-5 rounded-2xl bg-white shadow-md m-5 pb-5 hover:scale-105 hover:shadow-lg transition-all duration-500 group">
            <div className="overflow-hidden">
                <img className="rounded-t-2xl group-hover:scale-110 delay-75 transition-transform duration-500" src={imagePath} alt={title} />
            </div>
            <div className="px-5 flex flex-col gap-2">
                <h3 className="text-xl">{title}</h3>
                <p className="text-sm text-secondary">{description}</p>
            </div>
            <div className="flex items-center justify-between px-5">
                <h3 className="text-lg font-light text-secondary tracking-wide">$ {price.toFixed(2)}</h3>
                <Button onClick={handleAddToCart} className="cursor-pointer hover:bg-secondary/80 px-4 py-4 bg-secondary rounded-full"><Plus />Add to cart</Button>
            </div>
        </article>
    )
}