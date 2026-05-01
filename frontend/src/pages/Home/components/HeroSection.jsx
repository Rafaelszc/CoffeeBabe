import { Button } from "@/components/ui/button"
import { AuthContext } from "@/context/AuthContext"
import { useContext } from "react"
import { Link } from "react-router-dom"

export const HeroSection = () => {
  const { hasUser } = useContext(AuthContext)

  return (
    <main className="relative overflow-hidden bg-primary">
        <div className="flex items-center py-28 px-20">
            <div className="w-full text-start flex flex-col gap-5 animate-fade-in">
                <span className="text-secondary font-light tracking-widest text-sm">ARTESANAL COFFEE</span>
                <h1 className="text-6xl max-md:text-5xl text-secondary-foreground font-semibold tracking-wide md:w-3/4">
                    The <span className="text-secondary">flavor</span> to start your morning.
                </h1>
                <h2 className="text-sm text-secondary font-light md:w-3/4" style={{fontFamily: "Inter"}}>Carefully selected beans, signature roasts, and recipes that celebrate every moment. Wellcome to <span>CoffeeBabe</span>!</h2>
                <div className="flex gap-5 *:cursor-pointer">
                    <Button size="lg" className={"px-7 bg-secondary rounded-full hover:bg-secondary/90 transition-colors duration-300"}>
                        <Link to="/products">
                            See menu
                        </Link>
                    </Button>
                    {
                        !hasUser &&
                        <Button variant="outline" size="lg" className={"px-7 bg-white rounded-full transition-colors duration-300"}>
                            <Link to="/signup">
                                Sign Up
                            </Link>
                        </Button>
                    }
                </div>
            </div>
            <div className="w-full animate-scale-in max-md:hidden">
                <img className="rounded-2xl shadow-xl" src="src/assets/hero-coffee.jpg" alt="hero-coffee-image" />
            </div>
        </div>
    </main>
  )
}