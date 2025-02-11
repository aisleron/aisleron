package com.aisleron.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

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
        val homeBefore = db.locationDao().getHome()
        initializer.invoke()
        val homeAfter = db.locationDao().getHome()
        Assert.assertNull(homeBefore)
        Assert.assertNotNull(homeAfter)
    }

    @Test
    fun invoke_HomeLocationAdded_DefaultAisleCreated() = runTest {
        val aisleCountBefore = db.aisleDao().getAisles().count()
        initializer.invoke()

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