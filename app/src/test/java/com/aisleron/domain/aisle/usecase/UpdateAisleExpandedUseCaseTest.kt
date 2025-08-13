package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.GetLocationUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateAisleExpandedUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var updateAisleExpandedUseCase: UpdateAisleExpandedUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val aisleRepository = testData.getRepository<AisleRepository>()
        updateAisleExpandedUseCase = UpdateAisleExpandedUseCaseImpl(
            GetAisleUseCaseImpl(aisleRepository),
            UpdateAisleUseCaseImpl(
                aisleRepository,
                GetLocationUseCase(testData.getRepository<LocationRepository>()),
                IsAisleNameUniqueUseCase(aisleRepository)
            )
        )
    }

    @ParameterizedTest(name = "Test when Expanded is {0}")
    @MethodSource("expandedArguments")
    fun updateAisleExpanded_AisleExists_ExpandedUpdated(expanded: Boolean) = runTest {
        val existingAisle = testData.getRepository<AisleRepository>().getAll().first()
        val updatedAisle = updateAisleExpandedUseCase(existingAisle.id, expanded)

        assertNotNull(updatedAisle)
        assertEquals(existingAisle.id, updatedAisle?.id)
        assertEquals(existingAisle.name, updatedAisle?.name)
        assertEquals(expanded, updatedAisle?.expanded)
    }

    @Test
    fun updateAisleExpanded_AisleDoesNotExist_ReturnNull() = runTest {
        val updatedAisle = updateAisleExpandedUseCase(1001, true)
        assertNull(updatedAisle)
    }

    private companion object {
        @JvmStatic
        fun expandedArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}