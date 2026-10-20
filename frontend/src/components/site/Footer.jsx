import { Coffee, Mail } from "lucide-react"
import { Link } from "react-router-dom"
import { Container } from "./Container"
import githubLogo from "@/assets/github.png"
import linkedinLogo from "@/assets/linkedin.png"

const contacts = [
    {
        label: "rafaelszccontato@gmail.com",
        link: "mailto:rafaelszccontato@gmail.com",
        icon: <Mail className="size-4" />
    },
    {
        label: "github.com/rafaelszc",
        link: "https://github.com/rafaelszc",
        icon: <img src={githubLogo} alt="" className="size-4 dark:invert" />
    },
    {
        label: "linkedin.com/in/rafaelszc",
        link: "https://linkedin.com/in/rafaelszc",
        icon: <img src={linkedinLogo} alt="" className="size-4 dark:invert" />
    }
]

export const Footer = () => {
  return (
    <footer className="mt-24 border-t bg-secondary/50">
        <Container className="grid gap-10 py-14 md:grid-cols-[2fr_1fr_1fr]">
            <div className="flex max-w-sm flex-col gap-3">
                <Link to="/" className="flex items-center gap-2.5">
                    <Coffee className="size-5 text-primary" strokeWidth={1.75} />
                    <span className="text-lg font-semibold tracking-tight">CoffeeBabe</span>
                </Link>
                <p className="text-sm leading-relaxed text-muted-foreground">
                    Cafés, bebidas geladas e petiscos para retirar na loja ou receber em casa.
                </p>
            </div>
            <div className="flex flex-col gap-3">
                <h2 className="text-sm font-medium">Navegação</h2>
                <Link to="/products" className="text-sm text-muted-foreground transition-colors hover:text-foreground">Cardápio</Link>
                <Link to="/checkout" className="text-sm text-muted-foreground transition-colors hover:text-foreground">Carrinho</Link>
                <Link to="/profile" className="text-sm text-muted-foreground transition-colors hover:text-foreground">Minha conta</Link>
            </div>
            <div className="flex flex-col gap-3">
                <h2 className="text-sm font-medium">Contato</h2>
                {contacts.map((contact) => (
                    <a
                        key={contact.link}
                        href={contact.link}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-2 text-sm text-muted-foreground transition-colors hover:text-foreground"
                    >
                        {contact.icon}
                        <span className="break-all">{contact.label}</span>
                    </a>
                ))}
            </div>
        </Container>
        <div className="border-t">
            <Container className="flex flex-col gap-1 py-5 text-xs text-muted-foreground sm:flex-row sm:justify-between">
                <span>&copy; {new Date().getFullYear()} CoffeeBabe. Projeto de portfólio.</span>
                <span>Rafael Souza. Licença MIT.</span>
            </Container>
        </div>
    </footer>
  )
}
