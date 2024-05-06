package com.aisleron.domain.location.usecase

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveLocationUseCaseTest {

    @BeforeEach
    fun setUp() {
        println("Before")
    }

    @AfterEach
    fun tearDown() {
        println("After")
    }

    @Test
    operator fun invoke() {
        println("invoke")
    }
}