/*
 * Copyright (C) 2025-2026 aisleron.com
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

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.aisleproduct.AisleProductEntity
import com.aisleron.data.location.LocationDao
import com.aisleron.data.location.LocationEntity
import com.aisleron.data.loyaltycard.LocationLoyaltyCardDao
import com.aisleron.data.loyaltycard.LocationLoyaltyCardEntity
import com.aisleron.data.loyaltycard.LoyaltyCardDao
import com.aisleron.data.loyaltycard.LoyaltyCardEntity
import com.aisleron.data.maintenance.MaintenanceDao
import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteEntity
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity

@Database(
    entities = [
        AisleEntity::class,
        LocationEntity::class,
        ProductEntity::class,
        AisleProductEntity::class,
        LoyaltyCardEntity::class,
        LocationLoyaltyCardEntity::class,
        NoteEntity::class
    ],

    version = 7,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6)
        /** Migration from 6 to 7 is done manually to set initial rank values [MIGRATION_6_7] */
    ]
)
abstract class AisleronDatabase : AisleronDb, RoomDatabase() {
    abstract override fun aisleDao(): AisleDao
    abstract override fun locationDao(): LocationDao
    abstract override fun productDao(): ProductDao
    abstract override fun aisleProductDao(): AisleProductDao
    abstract override fun maintenanceDao(): MaintenanceDao
    abstract override fun loyaltyCardDao(): LoyaltyCardDao
    abstract override fun locationLoyaltyCardDao(): LocationLoyaltyCardDao
    abstract override fun noteDao(): NoteDao

    companion object {
        @JvmField
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Location ADD COLUMN expanded INTEGER NOT NULL DEFAULT 1")

                // Initial rank is based on id so locations are ranked by order of creation
                // Not doing this will lead to some really strange behavior when reordering
                db.execSQL("ALTER TABLE Location ADD COLUMN rank INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE Location SET rank = id")
            }
        }
    }
}