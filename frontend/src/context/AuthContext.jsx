import { checkUser } from "@/api/userService";
import { createContext, useEffect, useState } from "react";

export const AuthContext = createContext()

export const AuthProvider = ({ children }) => {
    const [hasUser, setHasUser] = useState(false)

    useEffect(() => {
        const checkCookies = async () => {
            const check = await checkUser()
            
            if (check.ok) setHasUser(true)
            else setHasUser(false)
        }

        checkCookies()
    }, [])
    return (
        <AuthContext.Provider value={{hasUser, setHasUser}}>
            {children}
        </AuthContext.Provider>
    )
}