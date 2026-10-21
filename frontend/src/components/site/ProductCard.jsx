import { Plus } from "lucide-react"
import { Button } from "../ui/button"
import { addToCart } from "@/api/cartService"
import { formatPrice, resolveImage } from "@/lib/format"
import { useAuth } from "@/context/auth"
import { useNavigate } from "react-router-dom"
import { toast } from "sonner"

export const ProductCard = ({ id, name, imageUrl, description, price, isLoading }) => {
  const { hasUser } = useAuth()
  const navigate = useNavigate()

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

  const handleAddToCart = async () => {
    if (!hasUser) {
      toast.info("Entre para montar seu carrinho.")
      navigate("/signin")
      return
    }
    try {
      await addToCart(id)
      toast.success(`${name} adicionado ao carrinho`)
    } catch {
      toast.error("Não foi possível adicionar agora. Tente novamente.")
    }
  }

  return (
    <article className="group flex h-full flex-col gap-4">
      <div className="overflow-hidden rounded-2xl bg-secondary">
        <img
          className="aspect-[4/3] w-full object-cover transition-transform duration-500 ease-out group-hover:scale-[1.04]"
          src={resolveImage(imageUrl)}
          alt={name}
          loading="lazy"
          // produto ainda sem imagem: mostra só o fundo
          onError={(e) => { e.currentTarget.style.visibility = "hidden" }}
        />
      </div>
      <div className="flex flex-1 flex-col gap-1.5">
        <h3 className="text-lg font-medium">{name}</h3>
        <p className="line-clamp-2 text-sm leading-relaxed text-muted-foreground">{description}</p>
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
