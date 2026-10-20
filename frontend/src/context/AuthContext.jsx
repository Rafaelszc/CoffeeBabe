import { fetchMe, signOutUser } from "@/api/userService";
import { useCallback, useEffect, useState } from "react";
import { AuthContext } from "./auth";

// A sessão é o cookie HttpOnly do backend; o frontend só pergunta quem está logado via /api/auth/me.
export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)

    const refreshUser = useCallback(async () => {
        try {
            setUser(await fetchMe())
        } catch {
            setUser(null)
        } finally {
            setLoading(false)
        }
    }, [])

    const signOut = useCallback(async () => {
        try {
            await signOutUser()
        } finally {
            setUser(null)
        }
    }, [])

    useEffect(() => {
        fetchMe()
            .then(setUser)
            .catch(() => setUser(null))
            .finally(() => setLoading(false))
    }, [])

    const value = { user, hasUser: !!user, isAdmin: user?.role === "ADMIN", loading, refreshUser, signOut }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}
