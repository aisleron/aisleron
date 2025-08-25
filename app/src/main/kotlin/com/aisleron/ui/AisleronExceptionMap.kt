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
            ExceptionCode.DUPLICATE_AISLE_NAME_EXCEPTION -> R.string.duplicate_aisle_name_exception
            ExceptionCode.INVALID_LOCATION_EXCEPTION -> R.string.invalid_location_exception
            ExceptionCode.INVALID_PRODUCT_EXCEPTION -> R.string.invalid_product_exception
            ExceptionCode.INVALID_DB_NAME_EXCEPTION -> R.string.invalid_db_name_exception
            ExceptionCode.INVALID_DB_BACKUP_FILE_EXCEPTION -> R.string.invalid_db_backup_file_exception
            ExceptionCode.INVALID_DB_RESTORE_FILE_EXCEPTION -> R.string.invalid_db_restore_file_exception
            ExceptionCode.DUPLICATE_PRODUCT_EXCEPTION -> R.string.duplicate_product_exception
            ExceptionCode.DUPLICATE_LOCATION_EXCEPTION -> R.string.duplicate_location_exception
            ExceptionCode.SAMPLE_DATA_CREATION_EXCEPTION -> R.string.sample_data_creation_exception
            ExceptionCode.LOYALTY_CARD_PROVIDER_EXCEPTION -> R.string.loyalty_card_provider_missing_exception
            ExceptionCode.INVALID_LOYALTY_CARD_EXCEPTION -> R.string.invalid_loyalty_card_exception
        }
    }
}