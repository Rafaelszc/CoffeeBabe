import { fetchCart } from "@/api/productService";
import { useEffect, useState } from "react";
import { ProductCartCard } from "./components/ProductCartCard";

export const Checkout = () => {
  const [cart, setCart] = useState(null);

  useEffect(() => {
    const getCart = async () => {
      const res = await fetchCart()

      if (res.ok) setCart(res.data)
      else console.error('Error fetching cart')
    
    };
    getCart();
  }, []);

  return (
    <div className="bg-white/50 px-20 py-10">
      <div className="grid grid-cols-3 gap-5">
        <div className="flex flex-col gap-5 col-span-2">
          {cart?.products?.length === 0 ? (
            <p className="text-secondary">Your cart is empty.</p>
          ) : (
            cart?.products?.map((item) => (
              <ProductCartCard {...item} />
            ))
            )
          }
        </div>
        <div className="w-full px-10 py-5 rounded-xl shadow-sm bg-white/80">
          <div className="flex flex-col gap-5">
            <h2 className="text-2xl">Order summary</h2>
            <div className="flex justify-between items-center">
              <span>Subtotal:</span>
              <span>${cart === null ? 0 : cart?.subtotal?.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center border-b">
              <span>Tax:</span>
              <span>${cart === null ? 0 : cart?.tax?.toFixed(2)}</span>
            </div>
            <div className="flex justify-between items-center">
              <h3 className="text-xl text-secondary">Total:</h3>
              <h3 className="text-xl text-secondary">${cart === null ? 0 : (cart?.tax+cart?.subtotal)}</h3>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}