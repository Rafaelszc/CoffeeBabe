import { Container } from "@/components/site/Container"
import { Button } from "@/components/ui/button"
import { Coffee } from "lucide-react"
import { Link } from "react-router-dom"

export const NotFound = () => {
  return (
    <Container className="flex min-h-[60dvh] flex-col items-center justify-center gap-5 py-20 text-center">
      <Coffee className="size-10 text-primary" strokeWidth={1.5} />
      <p className="text-sm font-medium text-muted-foreground">Erro 404</p>
      <h1 className="text-4xl font-semibold md:text-5xl">Página não encontrada</h1>
      <p className="max-w-[44ch] text-muted-foreground">O endereço pode ter mudado ou não existe mais.</p>
      <Button asChild size="lg" className="mt-2">
        <Link to="/">Voltar ao início</Link>
      </Button>
    </Container>
  )
}
