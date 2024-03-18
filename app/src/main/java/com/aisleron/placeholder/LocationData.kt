package com.aisleron.placeholder

import com.aisleron.model.FilterType
import com.aisleron.model.Location
import com.aisleron.model.LocationType

object LocationData {
    val locations: List<Location> =
        listOf(
            Location(
                id = 1,
                name = "Home",
                type = LocationType.GENERIC,
                defaultFilter = FilterType.NEEDED
            ),
            Location(
                id = 2,
                name = "New World Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED
            ),
            Location(
                id = 3,
                name = "Fresh Choice Cromwell",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED
            ),
            Location(
                id = 4,
                name = "Countdown Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED
            ),
            Location(
                id = 5,
                name = "4 Square Arrowtown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED
            ),
            Location(
                id = 6,
                name = "New World Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED
            ),
            Location(
                id = 7,
                name = "Torpedo 7 Queenstown",
                type = LocationType.SHOP,
                defaultFilter = FilterType.NEEDED
            )
        )
}
