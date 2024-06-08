package com.aisleron.ui

import com.aisleron.R
import com.aisleron.domain.base.AisleronException

class AisleronExceptionMap {
    fun getErrorResourceId(errorCode: String): Int {
        return when (errorCode) {
            AisleronException.DUPLICATE_PRODUCT_NAME_EXCEPTION -> R.string.duplicate_product_name_exception
            AisleronException.DUPLICATE_LOCATION_NAME_EXCEPTION -> R.string.duplicate_location_name_exception
            AisleronException.DELETE_DEFAULT_AISLE_EXCEPTION -> R.string.delete_default_aisle_exception
            AisleronException.INVALID_LOCATION_EXCEPTION -> R.string.invalid_location_exception
            else -> R.string.generic_error
        }
    }
}