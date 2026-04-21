import { Coffee, Search, ShoppingCart } from "lucide-react"
import { Input } from "../ui/input"
import { useState } from "react"
import { Link } from "react-router-dom"

const navItems = [
  {
    to: '/', label: 'Home',
  },
  {
    to: '/products', label: 'Products',
  },
  {
    to: '/signin', label: 'Sign In',
  }
]

export const Navbar = () => {
  const [query, setQuery] = useState('')
  const currentPath = window.location.pathname

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
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" style={{color: '#684d3b'}}/>
            <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search for coffee..."
            className="pl-9 w-56 bg-secondary/10 border-transparent focus-visible:bg-primary"
            />
          </form>
        </div>
        <Link className="hover:bg-secondary/10 p-3 rounded-sm transition-colors duration-300" to="/checkout">
          <ShoppingCart style={{color: '#684d3b'}}/>
        </Link>
      </div>
    </header>
  )
}