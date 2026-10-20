import { Navigate, Outlet, useLocation } from "react-router-dom"
import { useAuth } from "@/context/auth"

// Guarda de rota: sem sessão vai para /signin e volta depois; papel errado vai para a home.
export const RequireAuth = ({ role }) => {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return null
  if (!user) return <Navigate to="/signin" replace state={{ from: location.pathname }} />
  if (role && user.role !== role) return <Navigate to="/" replace />
  return <Outlet />
}
