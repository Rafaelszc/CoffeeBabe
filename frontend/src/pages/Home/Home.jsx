import { Navbar } from '../../components/site/Navbar'
import { BestSellers } from './components/BestSellers'
import { HeroSection } from './components/HeroSection'

export const Home = () => {
  return (
    <div>
      <Navbar />
      <HeroSection />
      <BestSellers />
    </div>
  )
}