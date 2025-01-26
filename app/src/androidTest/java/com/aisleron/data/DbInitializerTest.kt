package com.aisleron.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    fun invoke_NewDatabase_HomeLocationAdded() {
        val homeBefore = runBlocking { db.locationDao().getHome() }
        initializer.invoke()
        val homeAfter = runBlocking { db.locationDao().getHome() }
        Assert.assertNull(homeBefore)
        Assert.assertNotNull(homeAfter)
    }

    @Test
    fun invoke_HomeLocationAdded_DefaultAisleCreated() {
        val aisleCountBefore = runBlocking { db.aisleDao().getAisles().count() }
        initializer.invoke()
        val defaultAisle = runBlocking {
            val homeId = db.locationDao().getHome().id
            db.aisleDao().getDefaultAisleFor(homeId)
        }
        Assert.assertEquals(0, aisleCountBefore)
        Assert.assertNotNull(defaultAisle)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_DbInitializerReturned() {
        val init = DbInitializer(db.locationDao(), db.aisleDao())
        Assert.assertNotNull(init)
    }
}