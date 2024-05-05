package com.aisleron.domain.aisle

sealed class AisleException : Exception() {
    class DeleteDefaultAisleException : AisleException()
}