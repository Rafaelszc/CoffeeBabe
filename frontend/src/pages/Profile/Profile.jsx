import { checkUser, getCookieData, logoutUser } from "@/api/userService"
import { Button } from "@/components/ui/button"
import { AuthContext } from "@/context/AuthContext"
import { LogOut, Mail, User } from "lucide-react"
import { useContext, useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { toast } from "sonner"

export const Profile = () => {
    const [userData, setUserData] = useState({username: "Unknown", email: "Unkown@gmail.com"})
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
                toast.warning("User not found!")
            }
        }
        fetchUser()
    }, [])

    return (
        <div className="flex items-center justify-center w-full bg-secondary/10 py-20">
            <div className="px-15 py-15 bg-primary-foreground shadow-md rounded-2xl flex flex-col items-center gap-10">
                <div className="flex flex-col items-center gap-5">
                    <div className="bg-secondary text-white  text-3xl flex items-center justify-center h-28 w-28 shadow-md rounded-full">
                        <h2>{userData.username.slice(0, 2).toUpperCase()}</h2>
                    </div>
                    <h2 className="text-4xl">
                        {userData.username}
                    </h2>
                </div>
                <div className="w-full flex flex-col gap-5 m-10">
                    <div className="w-full flex gap-2 bg-secondary/20 px-5 py-2 rounded-2xl">
                        <div className="flex items-center justify-center">
                            <User className="text-secondary" />
                        </div>
                        <div className="flex-col flex">
                            <span className="font-light tracking-wider text-secondary-foreground text-sm">Username</span>
                            <span className="font-light">{userData.username}</span>
                        </div>
                    </div>
                    <div className="w-full flex gap-2 bg-secondary/20 px-5 py-2 rounded-2xl">
                        <div className="flex items-center justify-center">
                            <Mail className="text-secondary" />
                        </div>
                        <div className="flex-col flex">
                            <span className="font-light tracking-wider text-secondary-foreground text-sm">Email</span>
                            <span className="font-light">{userData.email}</span>
                        </div>
                    </div>
                    <div className="w-full bg-secondary">

                    </div>
                </div>
                <div className="w-full flex items-center justify-center">
                    <Button variant="destructive" onClick={() => handleLogout()} className="w-full p-5 cursor-pointer text-secondary">
                        <LogOut className="text-secondary" /> Logout
                    </Button>
                </div>
            </div>
        </div>
    )
}