import { BestSellers } from './components/BestSellers'
import { Categories } from './components/Categories'
import { Delivery } from './components/Delivery'
import { HeroSection } from './components/HeroSection'

export const Home = () => {
  return (
    <>
      <HeroSection />
      <BestSellers />
      <Categories />
      <Delivery />
    </>
  )
}
