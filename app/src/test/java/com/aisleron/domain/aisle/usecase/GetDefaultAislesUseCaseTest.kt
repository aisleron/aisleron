package com.aisleron.domain.aisle.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetDefaultAislesUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getDefaultAislesUseCase: GetDefaultAislesUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getDefaultAislesUseCase = GetDefaultAislesUseCase(testData.getRepository<AisleRepository>())
    }

    @Test
    fun getDefaultAisles_AislesReturned_MatchesRepoList() {
        val getAisleList: List<Aisle>
        val repoAisleList: List<Aisle>

        runBlocking {
            repoAisleList =
                testData.getRepository<AisleRepository>().getAll().filter { it.isDefault }
            getAisleList = getDefaultAislesUseCase()
        }

        Assertions.assertEquals(repoAisleList, getAisleList)
    }
}