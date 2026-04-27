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
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao : BaseDao<NoteEntity> {
    @Query("SELECT * FROM Note WHERE id = :noteId and (isRemoved = 0 or :includeRemoved = 1)")
    suspend fun getNote(noteId: Int, includeRemoved: Boolean = false): NoteEntity?

    @Query("SELECT * FROM Note where (isRemoved = 0 or :includeRemoved = 1)")
    suspend fun getNotes(includeRemoved: Boolean = false): List<NoteEntity>

    @Query("SELECT * FROM Note WHERE id IN (:ids) and (isRemoved = 0 or :includeRemoved = 1)")
    fun getNotes(ids: List<Int>, includeRemoved: Boolean = false): Flow<List<NoteEntity>>
}