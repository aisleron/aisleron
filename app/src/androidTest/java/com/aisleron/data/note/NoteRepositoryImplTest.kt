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

import com.aisleron.data.RepositoryImplTest
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.note.Note
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get

class NoteRepositoryImplTest : KoinTest, RepositoryImplTest<Note>() {

    override fun initRepository(): BaseRepository<Note> = NoteRepositoryImpl(
        noteDao = get<NoteDao>(), noteMapper = NoteMapper()
    )

    override suspend fun getSingleNewItem(): Note =
        Note(id = 0, noteText = "Add note 1")

    override suspend fun getMultipleNewItems(): List<Note> =
        listOf(
            Note(id = 0, noteText = "Add note 1"),
            Note(id = 0, noteText = "Add note 2")
        )

    override suspend fun getInvalidItem(): Note =
        Note(id = -1, noteText = "Dummy note")

    override fun getUpdatedItem(item: Note): Note =
        item.copy(noteText = "${item.noteText} Updated")

    @Test
    override fun add_SingleItemProvided_AddItem() {
        super.add_SingleItemProvided_AddItem()
    }
}