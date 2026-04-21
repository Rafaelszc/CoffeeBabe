import './App.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { SignIn } from './pages/SignIn/SignIn'
import { SignUp } from './pages/SignUp/SignUp'
import { Home } from './pages/Home/Home'
import { Products } from './pages/Products/Products'
import { Checkout } from './pages/Checkout/Checkout'
import { NotFound } from './pages/NotFound/NotFound'
import { Layout } from './components/site/Layout'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path='/signin' element={<SignIn />} />
          <Route path='/signup' element={<SignUp />} />
          <Route path='/products' element={<Products />} />
          <Route path='/checkout' element={<Checkout />} />
          <Route path="/*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
