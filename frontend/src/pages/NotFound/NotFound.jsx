import { Navbar } from "@/components/site/Navbar"
import { FileWarning } from "lucide-react"

export const NotFound = () => {
  return (
    <div className="flex flex-col gap-10 items-center justify-center w-full bg-primary/60 min-h-screen">
      <div className="absolute w-full top-0">
        <Navbar />
      </div>
      <span className="font-semibold text-3xl tracking-widest text-secondary">Oops!! Page not found</span>
      <div className="flex items-center gap-5">
        <FileWarning className="text-secondary" size={50} />
        <span className="font-light tracking-widest text-secondary text-2xl">Error 404</span>
      </div>
    </div>
  )
}