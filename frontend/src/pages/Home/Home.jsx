import { BottomNavbar } from '@/components/site/BottomNavbar'
import { Navbar } from '../../components/site/Navbar'
import { BestSellers } from './components/BestSellers'
import { HeroSection } from './components/HeroSection'
import { Footer } from '@/components/site/Footer'

export const Home = () => {
  return (
    <div>
      <Navbar />
      <HeroSection />
      <BestSellers />
      <BottomNavbar />
      <Footer />
    </div>
  )
}