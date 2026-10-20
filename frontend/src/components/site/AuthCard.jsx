import { Coffee } from "lucide-react"

export const AuthCard = ({ title, subtitle, children }) => (
  <section className="flex justify-center px-4 py-12 md:py-20">
    <div className="w-full max-w-md rounded-2xl border bg-card p-6 shadow-[0_24px_60px_-36px_var(--shadow-tint)] sm:p-10">
      <div className="mb-8 flex flex-col items-center gap-2 text-center">
        <Coffee className="size-6 text-primary" strokeWidth={1.75} />
        <h1 className="text-3xl font-semibold">{title}</h1>
        <p className="text-muted-foreground">{subtitle}</p>
      </div>
      {children}
    </div>
  </section>
)
