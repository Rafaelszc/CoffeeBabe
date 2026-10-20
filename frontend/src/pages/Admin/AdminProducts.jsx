import { createProduct, deleteProduct, fetchProducts, updateProduct, uploadProductImage } from "@/api/productService"
import { Container } from "@/components/site/Container"
import { FormField } from "@/components/site/FormField"
import { Pager } from "@/components/site/Pager"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { CATEGORIES } from "@/lib/categories"
import { formatPrice, resolveImage } from "@/lib/format"
import { usePaged } from "@/lib/usePaged"
import { productSchema } from "@/schemas/productSchema"
import { zodResolver } from "@hookform/resolvers/zod"
import { ImageUp, Pencil, Trash2 } from "lucide-react"
import { useState } from "react"
import { useForm } from "react-hook-form"
import { toast } from "sonner"

const MAX_IMAGE_BYTES = 2 * 1024 * 1024
const EMPTY = { name: "", description: "", price: "", category: "" }
const fieldClass = "w-full rounded-xl border border-input bg-background px-3.5 py-2.5 text-base outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 aria-invalid:border-destructive dark:bg-input/30"

const fetchPage = (page) => fetchProducts({ page, size: 20 })
const categoryLabel = (value) => CATEGORIES.find((c) => c.value === value)?.label

export const AdminProducts = () => {
  const { page, setPage, items, totalPages, loading, failed, reload } = usePaged(fetchPage)
  const [editing, setEditing] = useState(null)
  // muda a URL das miniaturas depois de um upload, para o navegador buscar a imagem nova
  const [imageVersion, setImageVersion] = useState(0)
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(productSchema),
    defaultValues: EMPTY,
  })

  const startEdit = (product) => {
    setEditing(product)
    reset({ ...product, price: String(product.price).replace(".", ",") })
    window.scrollTo({ top: 0, behavior: "smooth" })
  }

  const stopEdit = () => {
    setEditing(null)
    reset(EMPTY)
  }

  const onSubmit = async (data) => {
    try {
      if (editing) await updateProduct(editing.id, data)
      else await createProduct(data)
      toast.success(editing ? "Produto atualizado." : "Produto criado. Envie a imagem na lista abaixo.")
      stopEdit()
      reload()
    } catch (error) {
      toast.error(error.status === 400 ? "Confira os dados. O nome precisa ser único." : "Não foi possível salvar agora.")
    }
  }

  const handleDelete = async (product) => {
    if (!window.confirm(`Excluir ${product.name}?`)) return
    try {
      await deleteProduct(product.id)
      toast.success("Produto excluído.")
      if (editing?.id === product.id) stopEdit()
    } catch {
      toast.error("Não foi possível excluir agora.")
    }
    reload()
  }

  const handleImage = async (product, file) => {
    if (!file) return
    if (file.size > MAX_IMAGE_BYTES) {
      toast.error("A imagem deve ter no máximo 2 MB.")
      return
    }
    try {
      await uploadProductImage(product.id, file)
      setImageVersion((v) => v + 1)
      toast.success("Imagem atualizada.")
    } catch {
      toast.error("Arquivo inválido. Use JPEG, PNG ou WebP de até 2 MB.")
    }
  }

  return (
    <Container className="max-w-4xl py-12 md:py-16">
      <h1 className="text-4xl font-semibold md:text-5xl">Produtos</h1>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-10 grid gap-3 rounded-2xl border bg-card p-5 sm:grid-cols-2 sm:p-6">
        <h2 className="text-xl font-semibold sm:col-span-2">{editing ? `Editar ${editing.name}` : "Novo produto"}</h2>
        <FormField id="name" label="Nome" registration={register("name")} error={errors.name?.message} />
        <FormField id="price" label="Preço (R$)" registration={register("price")} error={errors.price?.message} />
        <div className="flex flex-col gap-2">
          <Label htmlFor="category">Categoria</Label>
          <select id="category" {...register("category")} aria-invalid={!!errors.category} className={fieldClass}>
            <option value="">Escolha</option>
            {CATEGORIES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>
          <p className="min-h-5 text-sm text-destructive">{errors.category?.message}</p>
        </div>
        <div className="flex flex-col gap-2 sm:col-span-2">
          <Label htmlFor="description">Descrição</Label>
          <textarea id="description" rows={3} {...register("description")} aria-invalid={!!errors.description} className={fieldClass} />
          <p className="min-h-5 text-sm text-destructive">{errors.description?.message}</p>
        </div>
        <div className="flex gap-3 sm:col-span-2">
          <Button type="submit" disabled={isSubmitting}>{editing ? "Salvar alterações" : "Criar produto"}</Button>
          {editing && <Button type="button" variant="outline" onClick={stopEdit}>Cancelar</Button>}
        </div>
      </form>

      <div className="mt-10 flex flex-col gap-3">
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => <div key={i} className="h-24 animate-pulse rounded-2xl bg-secondary" />)
        ) : failed ? (
          <p className="rounded-2xl bg-secondary px-6 py-12 text-center text-muted-foreground">Não foi possível carregar os produtos.</p>
        ) : (
          items.map((product) => (
            <article key={product.id} className="flex flex-wrap items-center gap-4 rounded-2xl border bg-card p-3 pr-4">
              <img
                src={`${resolveImage(product.imageUrl)}?v=${imageVersion}`}
                alt={product.name}
                className="size-16 shrink-0 rounded-xl bg-secondary object-cover"
              />
              <div className="min-w-0 flex-1">
                <h2 className="truncate font-medium">{product.name}</h2>
                <p className="text-sm text-muted-foreground">
                  {categoryLabel(product.category)} · <span className="tabular-nums">{formatPrice(product.price)}</span>
                </p>
              </div>
              <div className="flex gap-1">
                <Button asChild variant="ghost" size="icon">
                  <label aria-label={`Enviar imagem de ${product.name}`} className="cursor-pointer">
                    <ImageUp />
                    <input
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      className="sr-only"
                      onChange={(e) => {
                        handleImage(product, e.target.files?.[0])
                        e.target.value = ""
                      }}
                    />
                  </label>
                </Button>
                <Button variant="ghost" size="icon" aria-label={`Editar ${product.name}`} onClick={() => startEdit(product)}>
                  <Pencil />
                </Button>
                <Button variant="ghost" size="icon" aria-label={`Excluir ${product.name}`} onClick={() => handleDelete(product)} className="hover:text-destructive">
                  <Trash2 />
                </Button>
              </div>
            </article>
          ))
        )}
      </div>

      <Pager page={page} totalPages={totalPages} onChange={setPage} />
    </Container>
  )
}
