package com.aisleron.placeholder

import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisleproduct.AisleProduct
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
                            AisleProduct(100, 1, ProductData.products[1], 1),
                            AisleProduct(200, 1, ProductData.products[2], 2),
                            AisleProduct(300, 1, ProductData.products[3], 3),
                            AisleProduct(400, 1, ProductData.products[4], 4)
                        ),
                        1,
                        2,
                        1,
                        false
                    ),
                    Aisle(
                        "Pantry",
                        listOf(
                            AisleProduct(100, 2, ProductData.products[5], 5),
                            AisleProduct(200, 2, ProductData.products[6], 6),
                            AisleProduct(300, 2, ProductData.products[7], 7),
                            AisleProduct(400, 2, ProductData.products[8], 8)
                        ),
                        1,
                        1,
                        2,
                        true
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
                    Aisle("One", productsRandom(2), 2, 2, 3, false),
                    Aisle("Two", productsRandom(2), 2, 1, 4, true)
                )
            ),
            Location(
                id = 3,
                name = "Fresh Choice Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = true,
                aisles = listOf(
                    Aisle("Three", productsRandom(3), 3, 2, 5, false),
                    Aisle("Four", productsRandom(3), 3, 1, 6, true)
                )
            ),
            Location(
                id = 4,
                name = "Countdown Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                aisles = listOf(
                    Aisle("Five", productsRandom(4), 4, 2, 7, false),
                    Aisle("Six", productsRandom(4), 4, 1, 8, true)
                )
            ),
            Location(
                id = 5,
                name = "4 Square Arrowtown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = true,
                aisles = listOf(
                    Aisle("Seven", productsRandom(5), 5, 2, 9, false),
                    Aisle("Eight", productsRandom(5), 5, 1, 10, true)
                )
            ),
            Location(
                id = 6,
                name = "New World Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                aisles = listOf(
                    Aisle("Nine", productsRandom(6), 6, 2, 11, false),
                    Aisle("Ten", productsRandom(6), 6, 1, 12, true)
                )
            ),
            Location(
                id = 7,
                name = "Torpedo 7 Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                pinned = false,
                aisles = listOf(
                    Aisle("One", productsRandom(7), 7, 2, 13, false),
                    Aisle("Two", productsRandom(7), 7, 1, 14, true)
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
            result.add(AisleProduct(cnt * 100, aisleId, product, cnt))
        }
        return result
    }
}
