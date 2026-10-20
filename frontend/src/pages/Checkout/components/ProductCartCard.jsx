import { Button } from "@/components/ui/button"
import { formatPrice, resolveImage } from "@/lib/format"
import { Minus, Plus, Trash2 } from "lucide-react"

export const ProductCartCard = ({ product, quantity, lineTotal, onIncrease, onDecrease, onRemove, disabled }) => {
    const { name, description, imageUrl } = product

    return (
        <article className="flex items-center gap-4 rounded-2xl border bg-card p-3 pr-4 sm:gap-5">
            <img className="size-20 shrink-0 rounded-xl bg-secondary object-cover sm:size-24" src={resolveImage(imageUrl)} alt={name} />
            <div className="min-w-0 flex-1">
                <h2 className="truncate text-lg font-medium">{name}</h2>
                <p className="line-clamp-1 text-sm text-muted-foreground">{description}</p>
                <div className="mt-2 flex items-center gap-2">
                    <Button onClick={onDecrease} disabled={disabled} variant="outline" size="icon" aria-label={`Diminuir ${name}`} className="size-8">
                        <Minus />
                    </Button>
                    <span aria-label={`Quantidade de ${name}`} className="w-6 text-center tabular-nums">{quantity}</span>
                    <Button onClick={onIncrease} disabled={disabled} variant="outline" size="icon" aria-label={`Aumentar ${name}`} className="size-8">
                        <Plus />
                    </Button>
                </div>
            </div>
            <div className="flex flex-col items-end gap-2">
                <p className="font-medium tabular-nums">{formatPrice(lineTotal)}</p>
                <Button onClick={onRemove} disabled={disabled} variant="ghost" size="icon" aria-label={`Remover ${name} do carrinho`} className="text-muted-foreground hover:text-destructive">
                    <Trash2 />
                </Button>
            </div>
        </article>
    )
}
