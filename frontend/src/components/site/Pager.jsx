import { Button } from "@/components/ui/button"
import { ChevronLeft, ChevronRight } from "lucide-react"

// Paginação das listagens da API (page começa em 0).
export const Pager = ({ page, totalPages, onChange }) => {
  if (totalPages <= 1) return null

  return (
    <nav aria-label="Paginação" className="mt-10 flex items-center justify-center gap-4">
      <Button variant="outline" disabled={page === 0} onClick={() => onChange(page - 1)}>
        <ChevronLeft /> Anterior
      </Button>
      <span className="text-sm tabular-nums text-muted-foreground">Página {page + 1} de {totalPages}</span>
      <Button variant="outline" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Próxima <ChevronRight />
      </Button>
    </nav>
  )
}
