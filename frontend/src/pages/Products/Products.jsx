import { fetchProducts } from "@/api/productService";
import { ProductCard } from "@/components/site/ProductCard";
import { useEffect, useState } from "react";
import { motion } from "framer-motion";

export const Products = () => {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await fetchProducts();
        setProducts(data);
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="bg-white/20 gap-8 py-10">
      <div>

      </div>
      <div className="w-full flex items-center justify-center">
        <div className="flex flex-wrap w-4/5 justify-start">
          {products.length === 0 ? (
            <p className="text-secondary">No products available.</p>
          ) : (
          products.map((p) => (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.7 }}
              viewport={{once: true}}
            >
              <ProductCard {...p}/>
            </motion.div>
          )
          ))}
        </div>
      </div>
    </div>
  )
}