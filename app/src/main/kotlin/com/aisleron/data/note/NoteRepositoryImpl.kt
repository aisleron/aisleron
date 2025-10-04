/*
 * Copyright (C) 2025 aisleron.com
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

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val noteMapper: NoteMapper
) : NoteRepository {
    override suspend fun get(id: Int): Note? {
        return noteDao.getNote(id)?.let { noteMapper.toModel(it) }
    }

    override suspend fun getAll(): List<Note> {
        return noteMapper.toModelList(noteDao.getNotes())
    }

    override suspend fun add(item: Note): Int {
        return noteDao.upsert(noteMapper.fromModel(item)).single().toInt()
    }

    override suspend fun add(items: List<Note>): List<Int> {
        return upsertNotes(items)
    }

    override suspend fun update(item: Note) {
        noteDao.upsert(noteMapper.fromModel(item))
    }

    override suspend fun update(items: List<Note>) {
        upsertNotes(items)
    }

    override suspend fun remove(item: Note) {
        noteDao.delete(noteMapper.fromModel(item))
    }

    private suspend fun upsertNotes(notes: List<Note>): List<Int> {
        // '*' is a spread operator required to pass vararg down
        return noteDao
            .upsert(*noteMapper.fromModelList(notes).map { it }.toTypedArray())
            .map { it.toInt() }
    }
}