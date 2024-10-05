package com.aisleron.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.aisleron.data.maintenance.DatabaseMaintenanceImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before

class DatabaseMaintenanceImplTest {
    private lateinit var db: AisleronDatabase
    private lateinit var dbMaintenance: DatabaseMaintenanceImpl


    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AisleronDatabase::class.java
        ).build()

        val testDispatcher = UnconfinedTestDispatcher()

        dbMaintenance = DatabaseMaintenanceImpl(
            db,
            InstrumentationRegistry.getInstrumentation().context,
            testDispatcher
        )
    }

    /*    @Test
        fun getDatabaseName() {
        }

        @Test
        fun backupDatabase_OutputStreamIsEmpty_ThrowsInvalidDbBackupFileException() {
            val uri =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toURI()

            Assert.assertThrows(AisleronException.InvalidDbBackupFileException::class.java) {
                runBlocking {
                    dbMaintenance.backupDatabase(URI(uri.toString()), "")
                }
            }
        }

        @Test
        fun restoreDatabase() {
        }*/
}