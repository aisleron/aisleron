package com.aisleron.ui

import com.aisleron.R
import com.aisleron.domain.base.AisleronException.ExceptionCode

class AisleronExceptionMap {
    fun getErrorResourceId(errorCode: ExceptionCode): Int {
        return when (errorCode) {
            ExceptionCode.GENERIC_EXCEPTION -> R.string.generic_error
            ExceptionCode.DELETE_DEFAULT_AISLE_EXCEPTION -> R.string.delete_default_aisle_exception
            ExceptionCode.DUPLICATE_PRODUCT_NAME_EXCEPTION -> R.string.duplicate_product_name_exception
            ExceptionCode.DUPLICATE_LOCATION_NAME_EXCEPTION -> R.string.duplicate_location_name_exception
            ExceptionCode.INVALID_LOCATION_EXCEPTION -> R.string.invalid_location_exception
            ExceptionCode.INVALID_DB_NAME_EXCEPTION -> R.string.invalid_db_name_exception
            ExceptionCode.INVALID_DB_BACKUP_FILE_EXCEPTION -> R.string.invalid_db_backup_file_exception
            ExceptionCode.INVALID_DB_RESTORE_FILE_EXCEPTION -> R.string.invalid_db_restore_file_exception
        }
    }
}