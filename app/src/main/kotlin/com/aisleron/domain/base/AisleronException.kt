package com.aisleron.domain.base

sealed class AisleronException(
    val exceptionCode: String,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    class DeleteDefaultAisleException(message: String? = null, cause: Throwable? = null) :
        AisleronException(DELETE_DEFAULT_AISLE_EXCEPTION, message, cause)

    class DuplicateProductNameException(message: String? = null, cause: Throwable? = null) :
        AisleronException(DUPLICATE_PRODUCT_NAME_EXCEPTION, message, cause)

    class DuplicateLocationNameException(message: String? = null, cause: Throwable? = null) :
        AisleronException(DUPLICATE_LOCATION_NAME_EXCEPTION, message, cause)

    companion object {
        const val GENERIC_EXCEPTION = "generic_exception"
        const val DELETE_DEFAULT_AISLE_EXCEPTION = "delete_default_aisle_exception"
        const val DUPLICATE_PRODUCT_NAME_EXCEPTION = "duplicate_product_name_exception"
        const val DUPLICATE_LOCATION_NAME_EXCEPTION = "duplicate_location_name_exception"
    }
}