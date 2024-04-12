package com.aisleron.placeholder

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.AisleProduct
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationType

object LocationData {
    val locations: MutableList<Location> =
        mutableListOf(
            Location(
                id = 1,
                name = "Home",
                type = LocationType.HOME,
                defaultFilter = FilterType.NEEDED,
                pinned = true,
                aisles = listOf(
                    Aisle(
                        "Fridge",
                        listOf(
                            AisleProduct(100, 1, ProductData.products[1]),
                            AisleProduct(200, 1, ProductData.products[2]),
                            AisleProduct(300, 1, ProductData.products[3]),
                            AisleProduct(400, 1, ProductData.products[4])
                        ),
                        null,
                        2,
                        1
                    ),
                    Aisle(
                        "Pantry",
                        listOf(
                            AisleProduct(100, 2, ProductData.products[5]),
                            AisleProduct(200, 2, ProductData.products[6]),
                            AisleProduct(300, 2, ProductData.products[7]),
                            AisleProduct(400, 2, ProductData.products[8])
                        ),
                        null,
                        1,
                        2
                    )
                )
            ),
            Location(
                id = 2,
                name = "New World Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = true,
                aisles = listOf(
                    Aisle("One", productsRandom(2), null, 2, 3),
                    Aisle("Two", productsRandom(2), null, 1, 4)
                )
            ),
            Location(
                id = 3,
                name = "Fresh Choice Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = true,
                aisles = listOf(
                    Aisle("Three", productsRandom(3), null, 2, 5),
                    Aisle("Four", productsRandom(3), null, 1, 6)
                )
            ),
            Location(
                id = 4,
                name = "Countdown Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                aisles = listOf(
                    Aisle("Five", productsRandom(4), null, 2, 7),
                    Aisle("Six", productsRandom(4), null, 1, 8)
                )
            ),
            Location(
                id = 5,
                name = "4 Square Arrowtown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = true,
                aisles = listOf(
                    Aisle("Seven", productsRandom(5), null, 2, 9),
                    Aisle("Eight", productsRandom(5), null, 1, 10)
                )
            ),
            Location(
                id = 6,
                name = "New World Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                aisles = listOf(
                    Aisle("Nine", productsRandom(6), null, 2, 11),
                    Aisle("Ten", productsRandom(6), null, 1, 12)
                )
            ),
            Location(
                id = 7,
                name = "Torpedo 7 Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                aisles = listOf(
                    Aisle("One", productsRandom(7), null, 2, 13),
                    Aisle("Two", productsRandom(7), null, 1, 14)
                )
            )
        )

    private fun productsRandom(aisleId: Int): List<AisleProduct> {
        val products =
            ProductData.products.filter { p -> (p.id.mod((1..ProductData.products.count()).random())) == 0 }
        val result = mutableListOf<AisleProduct>()
        var cnt = 0
        for (product in products) {
            cnt += cnt
            result.add(AisleProduct(cnt * 100, aisleId, product))
        }
        return result
    }
}
