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
    <div className="bg-white/20 flex flex-col gap-10 py-10">
      <div className="px-20 flex flex-col gap-5">
        <h2 className="text-5xl text-secondary-foreground block">Our products</h2>
        <p className="text-secondary font-light tracking-wider">Discover specialty coffees, signature drinks, and desserts for every occasion.</p>
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