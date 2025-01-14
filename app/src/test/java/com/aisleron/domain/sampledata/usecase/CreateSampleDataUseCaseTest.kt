package com.aisleron.domain.sampledata.usecase

import com.aisleron.data.AisleronDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateSampleDataUseCaseTest {

    private lateinit var database: AisleronDatabase

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        /*val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            //ApplicationProvider.getApplicationContext<Context>(),
            AisleronDatabase::class.java,
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DbInitializer(database, testScope).invoke()
            }
        }).build()*/
    }

    @AfterEach
    fun tearDown() {
        //database.close()
    }

    @Test
    fun test_db_populate() {
        /*val locationRepository = LocationRepositoryImpl(database.locationDao(), LocationMapper())
        val getHomeLocationUseCase = GetHomeLocationUseCase(locationRepository)
        val location = runBlocking { getHomeLocationUseCase() }
        Assertions.assertNotNull(location)*/
    }

    /**
     * - Exception when products exists in database
     * - Products created
     * - Home Aisles Created
     * - Products moved in Home
     * - Shop created
     * - Products moved in Shop
     */
}