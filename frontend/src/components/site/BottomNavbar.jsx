import { Coffee, Home, LogIn, ShoppingBag, User } from "lucide-react"
import { useContext } from "react"
import { NavLink } from "react-router-dom"
import { AuthContext } from "@/context/AuthContext"
import { cn } from "@/lib/utils"

export const BottomNavbar = () => {
  const { hasUser } = useContext(AuthContext)

  const navItems = [
    { to: "/", label: "Início", icon: Home },
    { to: "/products", label: "Cardápio", icon: Coffee },
    { to: "/checkout", label: "Carrinho", icon: ShoppingBag },
    hasUser
      ? { to: "/profile", label: "Conta", icon: User }
      : { to: "/signin", label: "Entrar", icon: LogIn },
  ]

  return (
    <nav
      aria-label="Navegação inferior"
      className="fixed inset-x-0 bottom-0 z-40 border-t bg-background/90 pb-[env(safe-area-inset-bottom)] backdrop-blur-md md:hidden"
    >
      <ul className="grid h-16 grid-cols-4">
        {navItems.map(({ to, label, icon }) => {
          const Icon = icon
          return (
          <li key={to}>
            <NavLink
              to={to}
              end
              className={({ isActive }) => cn(
                "flex h-full flex-col items-center justify-center gap-1 text-xs transition-colors",
                isActive ? "text-primary" : "text-muted-foreground"
              )}
            >
              <Icon className="size-5" strokeWidth={1.75} />
              {label}
            </NavLink>
          </li>
        )})}
      </ul>
    </nav>
  )
}
