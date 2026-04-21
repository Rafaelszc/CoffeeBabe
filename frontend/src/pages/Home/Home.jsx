import { BottomNavbar } from '@/components/site/BottomNavbar'
import { BestSellers } from './components/BestSellers'
import { HeroSection } from './components/HeroSection'

export const Home = () => {
  return (
    <div>
      <HeroSection />
      <BestSellers />
      <BottomNavbar />
    </div>
  )
}