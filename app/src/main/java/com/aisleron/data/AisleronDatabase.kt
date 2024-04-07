package com.aisleron.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.aisle.AisleProductEntity
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity

@Database(
    entities = [AisleEntity::class, LocationEntity::class, ProductEntity::class, AisleProductEntity::class],
    version = 1
)
abstract class AisleronDatabase : RoomDatabase() {
    abstract fun aisleDao(): AisleDao
    abstract fun locationDao(): LocationDao
    abstract fun productDao(): ProductDao

    /*
    companion object {
        private var INSTANCE: AisleronDatabase? = null
        fun getDatabase(context: Context): AisleronDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE =
                        Room.databaseBuilder(
                            context,
                            AisleronDatabase::class.java,
                            "aisleron_database"
                        )
                            .build()
                }
            }
            return INSTANCE!!
        }
    }

     */
}