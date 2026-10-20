import { createContext, useContext } from "react"

export const AuthContext = createContext(null)

// { user, hasUser, isAdmin, loading, refreshUser, signOut }
export const useAuth = () => useContext(AuthContext)
