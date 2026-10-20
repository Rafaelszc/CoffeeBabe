import { checkUser, getCookieData, logoutUser } from "@/api/userService"
import { Button } from "@/components/ui/button"
import { AuthContext } from "@/context/AuthContext"
import { LogOut, Mail, User } from "lucide-react"
import { useContext, useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { Container } from "@/components/site/Container"
import { toast } from "sonner"

export const Profile = () => {
    const [userData, setUserData] = useState({username: "Visitante", email: ""})
    const navigate = useNavigate()
    const { setHasUser } = useContext(AuthContext)

    const handleLogout = () => {
        logoutUser()
        setHasUser(false)
        
        navigate("/")
    }

    useEffect(() => {
        const fetchUser = async () => {
            const check = await checkUser()
            if (check.ok) {
                const data = JSON.parse(getCookieData("userData"))
                setUserData(data)
            }
            else {
                navigate("/signin")
                toast.warning("Entre para ver sua conta.")
            }
        }
        fetchUser()
    }, [])

    const initials = userData.username.slice(0, 2).toUpperCase()

    return (
        <Container className="flex justify-center py-12 md:py-20">
            <div className="w-full max-w-md rounded-2xl border bg-card p-6 sm:p-10">
                <div className="flex flex-col items-center gap-4 text-center">
                    <div aria-hidden="true" className="flex size-20 items-center justify-center rounded-full bg-primary text-2xl font-semibold text-primary-foreground">
                        {initials}
                    </div>
                    <h1 className="text-3xl font-semibold">{userData.username}</h1>
                </div>
                <dl className="mt-8 flex flex-col gap-3">
                    <div className="flex items-center gap-3 rounded-xl bg-secondary px-4 py-3">
                        <User className="size-5 text-primary" strokeWidth={1.75} />
                        <div className="flex flex-col">
                            <dt className="text-xs text-muted-foreground">Nome de usuário</dt>
                            <dd>{userData.username}</dd>
                        </div>
                    </div>
                    <div className="flex items-center gap-3 rounded-xl bg-secondary px-4 py-3">
                        <Mail className="size-5 text-primary" strokeWidth={1.75} />
                        <div className="flex min-w-0 flex-col">
                            <dt className="text-xs text-muted-foreground">E-mail</dt>
                            <dd className="truncate">{userData.email}</dd>
                        </div>
                    </div>
                </dl>
                <Button variant="outline" size="lg" onClick={() => handleLogout()} className="mt-8 w-full">
                    <LogOut /> Sair
                </Button>
            </div>
        </Container>
    )
}
