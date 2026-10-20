import { Bike, Store } from "lucide-react"
import { Container } from "@/components/site/Container"
import { Reveal } from "./Reveal"

const options = [
  {
    icon: Store,
    title: "Retire na loja",
    text: "Faça o pedido pelo site e busque no balcão, sem taxa. O pagamento é feito na retirada.",
  },
  {
    icon: Bike,
    title: "Receba em casa",
    text: "Entrega com taxa fixa de R$ 20,00. O pagamento é feito na entrega.",
  },
]

export const Delivery = () => {
  return (
    <section className="py-16 md:py-24">
      <Container>
        <div className="rounded-2xl bg-secondary px-6 py-12 md:px-14 md:py-16">
          <Reveal>
            <h2 className="max-w-xl text-3xl font-semibold md:text-4xl">Do jeito que for melhor para você</h2>
          </Reveal>
          <div className="mt-10 grid gap-10 md:grid-cols-2 md:gap-16">
            {options.map(({ icon, title, text }, i) => {
              const Icon = icon
              return (
              <Reveal key={title} delay={i * 0.08} className="flex gap-4">
                <Icon className="mt-1 size-6 shrink-0 text-primary" strokeWidth={1.75} />
                <div>
                  <h3 className="text-lg font-medium">{title}</h3>
                  <p className="mt-1.5 max-w-[42ch] leading-relaxed text-muted-foreground">{text}</p>
                </div>
              </Reveal>
            )})}
          </div>
        </div>
      </Container>
    </section>
  )
}
