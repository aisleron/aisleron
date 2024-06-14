package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAisleUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var getAisleUseCase: GetAisleUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getAisleUseCase = GetAisleUseCaseImpl(testData.aisleRepository)
    }

    @Test
    fun getAisle_NonExistentId_ReturnNull() {
        Assertions.assertNull(runBlocking { getAisleUseCase(2001) })
    }

    @Test
    fun getAisle_ExistingId_ReturnAisle() {
        val aisle = runBlocking { getAisleUseCase(1) }
        Assertions.assertNotNull(aisle)
        Assertions.assertEquals(1, aisle!!.id)
    }
}