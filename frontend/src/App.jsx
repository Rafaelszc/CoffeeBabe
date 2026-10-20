import './App.css'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { SignIn } from './pages/SignIn/SignIn'
import { SignUp } from './pages/SignUp/SignUp'
import { Home } from './pages/Home/Home'
import { Products } from './pages/Products/Products'
import { Checkout } from './pages/Checkout/Checkout'
import { NotFound } from './pages/NotFound/NotFound'
import { Layout } from './components/site/Layout'
import { RequireAuth } from './components/site/RequireAuth'
import { AuthProvider } from './context/AuthContext'
import { Profile } from './pages/Profile/Profile'
import { Orders } from './pages/Orders/Orders'
import { AdminProducts } from './pages/Admin/AdminProducts'
import { AdminOrders } from './pages/Admin/AdminOrders'

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Home />} />
            <Route path='/signin' element={<SignIn />} />
            <Route path='/signup' element={<SignUp />} />
            <Route path='/products' element={<Products />} />
            <Route element={<RequireAuth />}>
              <Route path='/checkout' element={<Checkout />} />
              <Route path='/profile' element={<Profile />} />
              <Route path='/orders' element={<Orders />} />
            </Route>
            <Route element={<RequireAuth role="ADMIN" />}>
              <Route path='/admin/products' element={<AdminProducts />} />
              <Route path='/admin/orders' element={<AdminOrders />} />
            </Route>
            <Route path="*" element={<NotFound />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
