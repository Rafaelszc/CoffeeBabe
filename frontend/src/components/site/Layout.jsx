import { Outlet } from "react-router-dom"
import { Navbar } from "./Navbar"
import { Footer } from "./Footer"
import { BottomNavbar } from "./BottomNavbar"
import { Toaster } from "@/components/ui/sonner"

export const Layout = () => {
    return (
        <div className="flex min-h-dvh flex-col bg-background pb-16 md:pb-0">
            <Navbar />
            <main className="flex-1">
                <Outlet />
            </main>
            <Footer />
            <BottomNavbar />
            <Toaster position="top-center" />
        </div>
    )
}
