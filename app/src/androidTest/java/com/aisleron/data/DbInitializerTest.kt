/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.lang.Thread.sleep

class DbInitializerTest {
    private lateinit var db: AisleronDatabase
    private lateinit var initializer: DbInitializer

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AisleronDatabase::class.java,
        ).build()

        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        initializer = DbInitializer(db.locationDao(), db.aisleDao(), testScope)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun invoke_NewDatabase_HomeLocationAdded() = runTest {
        val homeBefore = db.locationDao().getLocations().count { it.type == LocationType.HOME }
        initializer.invoke()
        sleep(100)
        val homeAfter = db.locationDao().getLocations().count { it.type == LocationType.HOME }
        Assert.assertEquals(0, homeBefore)
        Assert.assertEquals(1, homeAfter)
    }

    @Test
    fun invoke_HomeLocationAdded_DefaultAisleCreated() = runTest {
        val aisleCountBefore = db.aisleDao().getAisles().count()
        initializer.invoke()
        sleep(100)

        val homeId = db.locationDao().getHome().id
        val defaultAisle = db.aisleDao().getDefaultAisleFor(homeId)

        Assert.assertEquals(0, aisleCountBefore)
        Assert.assertNotNull(defaultAisle)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_DbInitializerReturned() {
        val init = DbInitializer(db.locationDao(), db.aisleDao())
        Assert.assertNotNull(init)
    }
}