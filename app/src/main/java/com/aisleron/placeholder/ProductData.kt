package com.aisleron.placeholder

import com.aisleron.domain.product.Product

object ProductData {
    val products: MutableList<Product> =
        mutableListOf(
            Product(
                id = 1,
                name = "Sausage Roll",
                inStock = false
            ),
            Product(
                id = 2,
                name = "Milk",
                inStock = true
            ),
            Product(
                id = 3,
                name = "Soap",
                inStock = false
            ),
            Product(
                id = 4,
                name = "Toothpaste",
                inStock = false
            ),
            Product(
                id = 5,
                name = "Rolls Royce",
                inStock = true
            ),
            Product(
                id = 6,
                name = "Cornflakes",
                inStock = false
            ),
            Product(
                id = 7,
                name = "Apples",
                inStock = true
            ),
            Product(
                id = 8,
                name = "Tomatoes",
                inStock = true
            ),
            Product(
                id = 9,
                name = "Juice",
                inStock = false
            ),
            Product(
                id = 10,
                name = "Bratwurst",
                inStock = false
            ),
        )
}
