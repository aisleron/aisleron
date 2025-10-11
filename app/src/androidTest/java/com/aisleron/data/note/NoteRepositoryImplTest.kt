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
import com.aisleron.domain.note.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NoteRepositoryImplTest : KoinTest, RepositoryImplTest() {
    private lateinit var repository: NoteRepositoryImpl

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        super.setup()

        repository = NoteRepositoryImpl(
            noteDao = get<NoteDao>(), noteMapper = NoteMapper()
        )
    }

    override suspend fun addSingleItem(): Int {
        val item = Note(id = 0, noteText = "Add note 1")
        return repository.add(item)
    }

    override suspend fun addMultipleItems(): List<Int> {
        val items = listOf(
            Note(id = 0, noteText = "Add note 1"),
            Note(id = 0, noteText = "Add note 2")
        )

        return repository.add(items)
    }

    @Test
    override fun add_SingleItemProvided_AddItem() = runTest {
        val countBefore = repository.getAll().count()

        val newId = addSingleItem()

        val newItem = repository.get(newId)
        assertNotNull(newItem)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore.inc(), countAfter)
    }

    @Test
    override fun add_MultipleItemsProvided_AddItems() = runTest {
        val countBefore = repository.getAll().count()

        val newIds = addMultipleItems()

        assertEquals(2, newIds.count())

        val countAfter = repository.getAll().count()
        assertEquals(countBefore + 2, countAfter)

        val newItemOne = repository.get(newIds.first())
        assertNotNull(newItemOne)

        val newItemTwo = repository.get(newIds.last())
        assertNotNull(newItemTwo)
    }

    @Test
    override fun get_ValidIdProvided_ReturnItem() = runTest {
        val itemIds = addMultipleItems()

        val item = repository.get(itemIds.first())

        assertNotNull(item)
    }

    @Test
    override fun get_InvalidIdProvided_ReturnNull() = runTest {
        addMultipleItems()

        val item = repository.get(-1)

        assertNull(item)
    }

    @Test
    override fun getAll_MultipleItemsExist_ReturnAll() = runTest {
        val itemIds = addMultipleItems()

        val items = repository.getAll()

        assertEquals(itemIds.count(), items.count())
    }

    @Test
    override fun update_SingleItemProvided_UpdatedItem() = runTest {
        val itemIds = addMultipleItems()
        val item = repository.get(itemIds.first())!!.copy(noteText = "Updated note text")

        repository.update(item)

        val updatedItem = repository.get(item.id)
        assertEquals(item.noteText, updatedItem?.noteText)
    }

    @Test
    override fun update_MultipleItemsProvided_UpdateItems() = runTest {
        val itemIds = addMultipleItems()
        val countBefore = repository.getAll().count()
        val itemOneBefore = repository.get(itemIds.first())!!
        val itemTwoBefore = repository.get(itemIds.last())!!
        val updateItems = listOf(
            itemOneBefore.copy(noteText = "${itemOneBefore.noteText} Updated"),
            itemTwoBefore.copy(noteText = "${itemTwoBefore.noteText} Updated")
        )

        repository.update(updateItems)

        val itemOneAfter = repository.get(itemOneBefore.id)
        assertEquals(
            itemOneBefore.copy(noteText = "${itemOneBefore.noteText} Updated"),
            itemOneAfter
        )

        val itemTwoAfter = repository.get(itemTwoBefore.id)
        assertEquals(
            itemTwoBefore.copy(noteText = "${itemTwoBefore.noteText} Updated"),
            itemTwoAfter
        )

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    override fun remove_ValidItemProvided_RemoveItem() = runTest {
        val itemId = addMultipleItems().first()
        val itemBefore = repository.get(itemId)!!
        val countBefore = repository.getAll().count()

        repository.remove(itemBefore)

        val itemAfter = repository.get(itemId)
        assertNull(itemAfter)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    override fun remove_InvalidItemProvided_NoItemsRemoved() = runTest {
        addMultipleItems()
        val countBefore = repository.getAll().count()
        val item = Note(
            id = -1,
            noteText = "Dummy note"
        )

        repository.remove(item)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }
}