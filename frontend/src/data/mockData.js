export const productsData = [{
    id: 1,
    name: "Cappuccino",
    image: {
      url: "src/assets/product-cappuccino.jpg",
      alt: "A creamy espresso-based drink with steamed milk.",
    },
    price: 4.00
  },
  {
    id: 2,
    name: "Mocha",
    image: {
      url: "src/assets/product-mocha.jpg",
      alt: "A rich, chocolate-flavored coffee drink.",
    },
    price: 4.50
  },
  {
    id: 3,
    name: "Cake",
    image: {
      url: "src/assets/product-cake.jpg",
      alt: "A delicious, moist cake perfect for any occasion.",
    },
    price: 3.75
  },
  {
    id: 4,
    name: "Croissant",
    image: {
      url: "src/assets/product-croissant.jpg",
      alt: "A flaky, buttery pastry perfect for breakfast.",
    },
    price: 2.50
  },
  {
    id: 5,
    name: "Brownie",
    image: {
      url: "src/assets/product-brownie.jpg",
      alt: "A rich, chocolatey treat perfect for dessert.",
    },
    price: 3.00
  },
  {
    id: 6,
    name: "Iced Latte",
    image: {
      url: "src/assets/product-iced-latte.jpg",
      alt: "A refreshing cold coffee drink with espresso and milk.",
    },
    price: 4.25
  },
  {
    id: 7,
    name: "Espresso",
    image: {
      url: "src/assets/product-espresso.jpg",
      alt: "A strong, bold coffee drink perfect for those who love a powerful kick.",
    },
    price: 3.50
  }
]

export let cartData = {
  subtotal: 9.75,
  tax: 0.78,
  products: [
    {
      id: 3,
      name: "Cake",
      image: {
        url: "src/assets/product-cake.jpg",
        alt: "A delicious, moist cake perfect for any occasion.",
      },
      price: 3.75
    },
    {
      id: 4,
      name: "Croissant",
      image: {
        url: "src/assets/product-croissant.jpg",
        alt: "A flaky, buttery pastry perfect for breakfast.",
      },
      price: 2.50
    },
    {
      id: 5,
      name: "Brownie",
      image: {
        url: "src/assets/product-brownie.jpg",
        alt: "A rich, chocolatey treat perfect for dessert.",
      },
      price: 3.00
    },
  ]
}

export const userData = {
  id: 1,
  username: "Rafael",
  email: "rafaelbjj84@gmail.com",
  token: "aaaaaaa",
  role: "admin"
}