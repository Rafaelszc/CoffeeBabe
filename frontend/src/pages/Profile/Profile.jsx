import { Button } from "@/components/ui/button"
import { useAuth } from "@/context/auth"
import { ClipboardList, LogOut, Mail, Package, Receipt, User } from "lucide-react"
import { Link, useNavigate } from "react-router-dom"
import { Container } from "@/components/site/Container"

// Rota protegida por RequireAuth: aqui o usuário sempre existe.
export const Profile = () => {
    const navigate = useNavigate()
    const { user, isAdmin, signOut } = useAuth()

    const handleLogout = async () => {
        await signOut()
        navigate("/")
    }

    const initials = user.username.slice(0, 2).toUpperCase()

    return (
        <Container className="flex justify-center py-12 md:py-20">
            <div className="w-full max-w-md rounded-2xl border bg-card p-6 sm:p-10">
                <div className="flex flex-col items-center gap-4 text-center">
                    <div aria-hidden="true" className="flex size-20 items-center justify-center rounded-full bg-primary text-2xl font-semibold text-primary-foreground">
                        {initials}
                    </div>
                    <h1 className="text-3xl font-semibold">{user.username}</h1>
                </div>
                <dl className="mt-8 flex flex-col gap-3">
                    <div className="flex items-center gap-3 rounded-xl bg-secondary px-4 py-3">
                        <User className="size-5 text-primary" strokeWidth={1.75} />
                        <div className="flex flex-col">
                            <dt className="text-xs text-muted-foreground">Nome de usuário</dt>
                            <dd>{user.username}</dd>
                        </div>
                    </div>
                    <div className="flex items-center gap-3 rounded-xl bg-secondary px-4 py-3">
                        <Mail className="size-5 text-primary" strokeWidth={1.75} />
                        <div className="flex min-w-0 flex-col">
                            <dt className="text-xs text-muted-foreground">E-mail</dt>
                            <dd className="truncate">{user.email}</dd>
                        </div>
                    </div>
                </dl>
                <div className="mt-8 flex flex-col gap-3">
                    <Button asChild size="lg">
                        <Link to="/orders"><Receipt /> Meus pedidos</Link>
                    </Button>
                    {isAdmin && (
                        <>
                            <Button asChild variant="outline" size="lg">
                                <Link to="/admin/products"><Package /> Gerenciar produtos</Link>
                            </Button>
                            <Button asChild variant="outline" size="lg">
                                <Link to="/admin/orders"><ClipboardList /> Gerenciar pedidos</Link>
                            </Button>
                        </>
                    )}
                    <Button variant="outline" size="lg" onClick={handleLogout}>
                        <LogOut /> Sair
                    </Button>
                </div>
            </div>
        </Container>
    )
}
