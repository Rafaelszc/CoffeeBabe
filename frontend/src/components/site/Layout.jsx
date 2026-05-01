import { Outlet } from "react-router-dom"
import { Navbar } from "./Navbar"
import { Footer } from "./Footer"
import { Toaster } from "sonner"

export const Layout = () => {
    return (
        <div className="min-h-screen flex flex-col bg-primary">
            <Navbar />
            <main className="flex-1">
                <Toaster />
                <Outlet />
            </main>
            <Footer />
        </div>
    )
}