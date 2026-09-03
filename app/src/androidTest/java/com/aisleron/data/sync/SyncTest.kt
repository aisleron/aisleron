/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.data.sync

import com.aisleron.data.base.SyncEntity
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.inMemoryDatabaseTestModule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class SyncTest<Entity : SyncEntity, Dto : SyncDto> : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> = listOf(
        daoModule, inMemoryDatabaseTestModule
    )

    protected lateinit var repository: SyncRepositoryImpl<Entity, Dto>
    protected lateinit var syncApi: SyncApiTestImpl<Dto>
    protected lateinit var mapper: DtoMapper<Entity, Dto>
    protected lateinit var dao: SyncDao<Entity>

    protected abstract fun initSyncApi(): SyncApiTestImpl<Dto>
    protected abstract fun initMapper(): DtoMapper<Entity, Dto>
    protected abstract fun initDao(): SyncDao<Entity>
    protected abstract suspend fun addEntity(
        lastModifiedAt: Long = 0,
        serverUpdatedAt: Long? = null,
        isRemoved: Boolean = false,
        syncId: String? = null
    ): Entity

    protected abstract suspend fun addDto(
        id: String,
        serverUpdatedAt: String? = null,
        clientUpdatedAt: String = "",
        isDeleted: Boolean = false
    ): Dto

    protected abstract suspend fun validateDtoToEntity(dto: Dto, compareEntity: Entity): Boolean

    @Before
    fun setUp() {
        dao = initDao()
        mapper = initMapper()
        syncApi = initSyncApi()

        repository = SyncRepositoryImpl(
            syncOrder = 1,
            dao = dao,
            syncApi = syncApi,
            dtoMapper = mapper
        )
    }

    protected suspend fun pushEntityTest(lastSyncTimestamp: Long, entity: Entity): Dto {
        val expectedDto = mapper.toDto(entity)

        repository.push(lastSyncTimestamp)

        val dto = syncApi.remoteDtoList.single()
        assertEquals(expectedDto, dto)

        return dto
    }

    protected suspend fun pullDtoTest(lastSyncIso: String, dto: Dto): Entity {
        val resultTimestamp = repository.pull(lastSyncIso)

        assertEquals(lastSyncIso, syncApi.fetchSinceArg)
        assertEquals("2026-08-18T05:00:00Z", resultTimestamp)

        val compareEntity = dao.getBySyncId(dto.id)
        assertNotNull(compareEntity)
        assertTrue(validateDtoToEntity(dto, compareEntity))

        return compareEntity
    }

    @Test
    fun push_LocalNewOrModifiedEntityExist_PushesMappedDtoToApi() = runTest {
        val lastSyncTimestamp = 1000L
        val entity = addEntity(lastModifiedAt = 1500L)

        pushEntityTest(lastSyncTimestamp, entity)
    }

    @Test
    fun pull_RemoteNewOrUpdatedEntityExist_UpsertsEntityAndReturnsLatestTimestamp() = runTest {
        val lastSyncIso = "2026-08-18T00:00:00Z"
        val id = "${repository.remoteEntityName}-pull-test"
        val dto = addDto(id, "2026-08-18T05:00:00Z", "2026-08-17T05:00:00Z")

        pullDtoTest(lastSyncIso, dto)
    }

    @Test
    fun purgeRemoved_PurgeToDateProvided_CallsDaoPurgeRemoved() = runTest {
        val purgeToDate = 5000L
        val entity = addEntity(
            lastModifiedAt = 5000L, isRemoved = true, syncId = SyncEntity.generateSyncId()
        )
        val syncId = entity.syncId!!

        assertNotNull(dao.getBySyncId(syncId))

        repository.purgeRemoved(purgeToDate)

        assertNull(dao.getBySyncId(syncId))

    }
}