import { Eye, EyeOff } from "lucide-react"
import { useState } from "react"
import { Input } from "../ui/input"
import { Label } from "../ui/label"

export const FormField = ({ id, label, type = "text", error, registration, autoComplete }) => {
  const [visible, setVisible] = useState(false)
  const isPassword = type === "password"

  return (
    <div className="flex flex-col gap-2">
      <Label htmlFor={id}>{label}</Label>
      <div className="relative">
        <Input
          id={id}
          type={isPassword && visible ? "text" : type}
          autoComplete={autoComplete}
          aria-invalid={!!error}
          aria-describedby={error ? `${id}-error` : undefined}
          className={isPassword ? "pr-11" : undefined}
          {...registration}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setVisible(!visible)}
            aria-label={visible ? "Ocultar senha" : "Mostrar senha"}
            className="absolute right-1.5 top-1/2 flex size-8 -translate-y-1/2 cursor-pointer items-center justify-center rounded-full text-muted-foreground transition-colors hover:text-foreground"
          >
            {visible ? <EyeOff className="size-4" /> : <Eye className="size-4" />}
          </button>
        )}
      </div>
      <p id={`${id}-error`} className="min-h-5 text-sm text-destructive">{error}</p>
    </div>
  )
}
