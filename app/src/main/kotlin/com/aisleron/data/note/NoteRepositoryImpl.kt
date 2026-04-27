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

import com.aisleron.domain.note.Note
import com.aisleron.domain.note.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val noteMapper: NoteMapper
) : NoteRepository {
    override suspend fun get(id: Int): Note? =
        getNote(id, false)

    override suspend fun getAll(): List<Note> {
        return noteMapper.toModelList(noteDao.getNotes())
    }

    override suspend fun add(item: Note): Int {
        return noteDao.upsert(noteMapper.fromModel(item, null)).single().toInt()
    }

    override suspend fun add(items: List<Note>): List<Int> {
        val notes = items.map { noteMapper.fromModel(it, null) }
        return upsertNotes(notes)
    }

    private suspend fun mapExisting(item: Note, includeDeleted: Boolean = false): NoteEntity {
        val currentEntity = noteDao.getNote(item.id, includeDeleted)
        return noteMapper.fromModel(item, currentEntity)
    }

    override suspend fun update(item: Note) {
        noteDao.upsert(mapExisting(item))
    }

    override suspend fun update(items: List<Note>) {
        val notes = items.map { mapExisting(it) }
        upsertNotes(notes)
    }

    override suspend fun remove(item: Note) {
        val removeEntity = mapExisting(item).copy(isRemoved = true)
        noteDao.upsert(removeEntity)
    }

    override suspend fun hardDelete(item: Note) {
        noteDao.delete(mapExisting(item, true))
    }

    private suspend fun upsertNotes(notes: List<NoteEntity>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return noteDao
            .upsert(*notes.toTypedArray())
            .map { it.toInt() }
    }

    override fun getMultiple(ids: List<Int>): Flow<List<Note>> {
        val noteEntities = noteDao.getNotes(ids)
        return noteEntities.map { noteMapper.toModelList(it) }
    }

    override suspend fun getRemoved(id: Int): Note? =
        getNote(id, true)

    private suspend fun getNote(id: Int, includeDeleted: Boolean): Note? {
        return noteDao.getNote(id, includeDeleted)?.let { noteMapper.toModel(it) }
    }
}