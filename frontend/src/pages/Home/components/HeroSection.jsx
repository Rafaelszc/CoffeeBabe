import { Button } from "@/components/ui/button"
import { Container } from "@/components/site/Container"
import { AuthContext } from "@/context/AuthContext"
import { motion } from "framer-motion"
import { useContext } from "react"
import { Link } from "react-router-dom"
import heroImage from "@/assets/hero-coffee.jpg"

const MotionDiv = motion.div

const ease = [0.16, 1, 0.3, 1]

export const HeroSection = () => {
  const { hasUser } = useContext(AuthContext)

  return (
    <section className="overflow-hidden">
      <Container className="grid min-h-[calc(100dvh-4rem)] items-center gap-10 py-12 md:grid-cols-[1.05fr_1fr] md:gap-16 md:py-16">
        <MotionDiv
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, ease }}
          className="order-2 flex flex-col gap-6 md:order-1"
        >
          <h1 className="text-4xl font-semibold leading-[1.05] md:text-5xl lg:text-6xl">
            O sabor que começa o seu dia.
          </h1>
          <p className="max-w-[46ch] text-lg leading-relaxed text-muted-foreground">
            Grãos selecionados, torras da casa e sobremesas feitas todos os dias. Peça para retirar ou receba em casa.
          </p>
          <div className="flex flex-wrap gap-3">
            <Button asChild size="lg">
              <Link to="/products">Ver cardápio</Link>
            </Button>
            {!hasUser && (
              <Button asChild size="lg" variant="outline">
                <Link to="/signup">Criar conta</Link>
              </Button>
            )}
          </div>
        </MotionDiv>
        <MotionDiv
          initial={{ opacity: 0, scale: 0.97 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.9, ease }}
          className="order-1 md:order-2"
        >
          <img
            src={heroImage}
            alt="Xícara de café sobre a mesa"
            fetchPriority="high"
            className="aspect-[16/10] w-full rounded-2xl object-cover shadow-[0_24px_60px_-28px_var(--shadow-tint)] md:aspect-[4/5]"
          />
        </MotionDiv>
      </Container>
    </section>
  )
}
