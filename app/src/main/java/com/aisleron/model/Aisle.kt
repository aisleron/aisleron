package com.aisleron.model

class Aisle: Base() {
    lateinit var products: List<Product>
    lateinit var location: Location
}