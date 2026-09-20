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

package com.aisleron.data.note

import androidx.room.Dao
import androidx.room.Query
import com.aisleron.data.base.BaseDao
import com.aisleron.data.sync.SyncDao
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao : BaseDao<NoteEntity>, SyncDao<NoteEntity> {
    @Query("SELECT * FROM Note WHERE id = :noteId AND (isRemoved = 0 OR :includeRemoved = 1)")
    suspend fun getNote(noteId: Int, includeRemoved: Boolean): NoteEntity?

    @Query("SELECT * FROM Note WHERE isRemoved = 0")
    suspend fun getNotes(): List<NoteEntity>

    @Query("SELECT * FROM Note WHERE id IN (:ids) AND isRemoved = 0")
    fun getNotes(ids: List<Int>): Flow<List<NoteEntity>>


    @Query("SELECT * FROM Note WHERE lastModifiedAt > :modifiedAfterDate")
    override suspend fun getModified(modifiedAfterDate: Long): List<NoteEntity>

    @Query("SELECT * FROM Note WHERE syncId = :syncId")
    override suspend fun getBySyncId(syncId: String): NoteEntity?

    @Query("DELETE FROM Note WHERE isRemoved = 1 AND lastModifiedAt <= :purgeToDate")
    override suspend fun purgeRemoved(purgeToDate: Long)

    @Query("SELECT * FROM Note WHERE noteText = :noteText COLLATE NOCASE AND createdAt = :createdAt")
    fun getByNaturalKey(noteText: String, createdAt: Long): List<NoteEntity>
}