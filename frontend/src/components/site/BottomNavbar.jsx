import { DoorOpen, Home, LucideAlarmClock, LucideSquareArrowRightEnter, ShoppingBag } from "lucide-react"
import { Link } from "react-router-dom"

const navItems = [
  {
    to: '/', label: 'Home', icon: <Home />
  },
  {
    to: '/products', label: 'Products', icon: <ShoppingBag />
  },
  {
    to: '/signin', label: 'Sign In', icon: <LucideSquareArrowRightEnter />
  }
]

export const BottomNavbar = () => {
  return (
    <div className="md:hidden fixed bottom-0 backdrop-blur-md border-t w-full h-16 bg-primary/80 flex items-center justify-center">
        <ul className="flex items-center gap-10">
            {navItems.map((item) => (
                <li key={item.to}>
                    <Link to={item.to} className="flex flex-col items-center text-secondary hover:text-secondary/80 transition-colors duration-300">
                        {item.icon}
                        {item.label}
                    </Link>
                </li>
            ))}
        </ul>
    </div>
  )
}