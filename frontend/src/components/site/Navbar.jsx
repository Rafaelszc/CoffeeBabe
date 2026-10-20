import { Coffee, Search, ShoppingBag, User } from "lucide-react"
import { Input } from "../ui/input"
import { Button } from "../ui/button"
import { useContext, useState } from "react"
import { Link, NavLink, useNavigate } from "react-router-dom"
import { AuthContext } from "@/context/AuthContext"
import { Container } from "./Container"
import { ThemeToggle } from "./ThemeToggle"
import { cn } from "@/lib/utils"

const navItems = [
  { to: "/", label: "Início" },
  { to: "/products", label: "Cardápio" },
]

export const Navbar = () => {
  const [query, setQuery] = useState("")
  const navigate = useNavigate()
  const { hasUser } = useContext(AuthContext)

  const handleSearch = (e) => {
    e.preventDefault()
    const q = query.trim()
    navigate(q ? `/products?q=${encodeURIComponent(q)}` : "/products")
  }

  return (
    <header className="sticky top-0 z-40 border-b bg-background/85 backdrop-blur-md">
      <Container className="flex h-16 items-center justify-between gap-6">
        <Link to="/" className="flex items-center gap-2.5 text-foreground">
          <Coffee className="size-6 text-primary" strokeWidth={1.75} />
          <span className="text-lg font-semibold tracking-tight">CoffeeBabe</span>
        </Link>

        <nav aria-label="Principal" className="max-md:hidden">
          <ul className="flex items-center gap-8">
            {navItems.map((item) => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end
                  className={({ isActive }) => cn(
                    "text-sm transition-colors duration-200 hover:text-foreground",
                    isActive ? "font-medium text-foreground" : "text-muted-foreground"
                  )}
                >
                  {item.label}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <div className="flex items-center gap-1">
          <form role="search" onSubmit={handleSearch} className="relative mr-2 max-lg:hidden">
            <Search className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <label htmlFor="nav-search" className="sr-only">Buscar no cardápio</label>
            <Input
              id="nav-search"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Buscar café ou petisco"
              className="h-10 w-60 rounded-full border-transparent bg-secondary pl-10"
            />
          </form>
          <ThemeToggle />
          <Button asChild variant="ghost" size="icon">
            <Link to="/checkout" aria-label="Carrinho">
              <ShoppingBag />
            </Link>
          </Button>
          {hasUser ? (
            <Button asChild variant="ghost" size="icon" className="max-md:hidden">
              <Link to="/profile" aria-label="Minha conta">
                <User />
              </Link>
            </Button>
          ) : (
            <Button asChild className="ml-2 max-md:hidden">
              <Link to="/signin">Entrar</Link>
            </Button>
          )}
        </div>
      </Container>
    </header>
  )
}
