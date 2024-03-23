package com.aisleron.placeholder

import com.aisleron.model.Aisle
import com.aisleron.model.FilterType
import com.aisleron.model.Location
import com.aisleron.model.LocationType
import com.aisleron.model.Product

object LocationData {
    val locations: List<Location> =
        listOf(
            Location(
                id = 1,
                name = "Home",
                type = LocationType.GENERIC,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("Fridge", productsRandom(), null, 2, 1),
                    Aisle ("Pantry", productsRandom(), null, 1, 2)
                )
            ),
            Location(
                id = 2,
                name = "New World Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("One", productsRandom(), null, 2, 3),
                    Aisle ("Two", productsRandom(), null, 1, 4)
                )
            ),
            Location(
                id = 3,
                name = "Fresh Choice Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("Three", productsRandom(), null, 2, 5),
                    Aisle ("Four", productsRandom(), null, 1, 6)
                )
            ),
            Location(
                id = 4,
                name = "Countdown Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("Five", productsRandom(), null, 2, 7),
                    Aisle ("Six", productsRandom(), null, 1, 8)
                )
            ),
            Location(
                id = 5,
                name = "4 Square Arrowtown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("Seven", productsRandom(), null, 2, 9),
                    Aisle ("Eight", productsRandom(), null, 1, 10)
                )
            ),
            Location(
                id = 6,
                name = "New World Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("Nine", productsRandom(), null, 2, 11),
                    Aisle ("Ten", productsRandom(), null, 1, 12 )
                )
            ),
            Location(
                id = 7,
                name = "Torpedo 7 Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED,
                aisles = listOf(
                    Aisle ("One", productsRandom(), null, 2, 13),
                    Aisle ("Two", productsRandom(), null, 1, 14)
                )
            )
        )

    private fun productsRandom() = ProductData.products.filter { p -> (p.id.mod((1..ProductData.products.count()).random())) == 0 }
}
