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

package com.aisleron.testdata.data.note

import com.aisleron.data.note.NoteDao
import com.aisleron.data.note.NoteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NoteDaoTestImpl : NoteDao {
    private val noteList = mutableListOf<NoteEntity>()
    private val activeItems: List<NoteEntity> get() = noteList.filter { !it.isRemoved }

    override suspend fun getNote(noteId: Int, includeRemoved: Boolean): NoteEntity? {
        return noteList.find { it.id == noteId && (!it.isRemoved || includeRemoved) }
    }

    override suspend fun getNotes(): List<NoteEntity> = activeItems

    override fun getNotes(ids: List<Int>): Flow<List<NoteEntity>> {
        val result = activeItems.filter { note -> ids.contains(note.id) }
        return flowOf(result)
    }

    override suspend fun upsert(vararg entity: NoteEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val existingEntity = getNote(it.id, true)
            val id = existingEntity?.let {
                noteList.removeAt(noteList.indexOf(existingEntity))
                existingEntity.id
            } ?: ((noteList.maxOfOrNull { e -> e.id } ?: 0) + 1)

            val newEntity = NoteEntity(
                id = id,
                noteText = it.noteText,
                syncId = it.syncId,
                isRemoved = it.isRemoved,
                lastModifiedAt = it.lastModifiedAt,
                serverUpdatedAt = it.serverUpdatedAt
            )

            noteList.add(newEntity)
            result.add(existingEntity?.let { -1 } ?: newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: NoteEntity) {
        entity.forEach { e ->
            noteList.removeIf { it.id == e.id }
        }
    }
}