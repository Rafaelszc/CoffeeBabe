import { Coffee, Search, ShoppingCart } from "lucide-react"
import { Input } from "../ui/input"
import { useContext, useState } from "react"
import { Link } from "react-router-dom"
import { AuthContext } from "@/context/AuthContext"

export const Navbar = () => {
  const [query, setQuery] = useState('')
  const currentPath = window.location.pathname
  const { hasUser } = useContext(AuthContext)

  const navItems = [{
      to: '/', label: 'Home',
      },
      {
        to: '/products', label: 'Products',
      }, hasUser
      ? {
        to: '/profile', label: 'Profile'
      }
      : {
        to: '/signin', label: 'Sign In',
      }
    ]

  return (
    <header className="w-full flex items-center justify-between bg-white/70 px-10 py-5 sticky top-0 z-50 backdrop-blur-md border-b">
      <div>
        <Link className="flex items-center justify-between gap-5" to={"/"}>
            <Coffee className="text-secondary"/>
            <h1 className="font-semibold text-2xl ">CoffeeBabe</h1>     
        </Link>
      </div>
      <nav className="max-md:hidden">
        <ul className="flex items-center justify-between gap-10">
          {navItems.map((item) => (
            <li key={item.to}>
              <Link to={item.to} className={`${currentPath === item.to ? 'text-secondary-foreground' : 'text-secondary'}  hover:text-secondary/80 transition-colors duration-300`}>
                {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
      <div className="flex items-center gap-5 max-md:hidden">
        <div>
          <form className="relative" action="">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-secondary"/>
            <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search for coffee..."
            className="pl-9 w-56 bg-secondary/10 border-transparent focus-visible:bg-primary"
            />
          </form>
        </div>
        <Link className="relative hover:bg-secondary/10 p-3 rounded-sm transition-colors duration-300" to="/checkout">
          <ShoppingCart className="text-secondary"/>
        </Link>
      </div>
    </header>
  )
}