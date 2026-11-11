/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.domain.base

sealed class AisleronException(
    val exceptionCode: ExceptionCode,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    class DeleteDefaultAisleException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.DELETE_DEFAULT_AISLE_EXCEPTION, message, cause)

    class DuplicateProductNameException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.DUPLICATE_PRODUCT_NAME_EXCEPTION, message, cause)

    class DuplicateLocationNameException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.DUPLICATE_LOCATION_NAME_EXCEPTION, message, cause)

    class DuplicateAisleNameException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.DUPLICATE_AISLE_NAME_EXCEPTION, message, cause)

    class InvalidLocationException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_LOCATION_EXCEPTION, message, cause)

    class InvalidProductException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_PRODUCT_EXCEPTION, message, cause)

    class InvalidDbNameException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_DB_NAME_EXCEPTION, message, cause)

    class InvalidDbBackupFileException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_DB_BACKUP_FILE_EXCEPTION, message, cause)

    class InvalidDbRestoreFileException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_DB_RESTORE_FILE_EXCEPTION, message, cause)

    class DuplicateProductException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.DUPLICATE_PRODUCT_EXCEPTION, message, cause)

    class DuplicateLocationException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.DUPLICATE_LOCATION_EXCEPTION, message, cause)

    class SampleDataCreationException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.SAMPLE_DATA_CREATION_EXCEPTION, message, cause)

    class LoyaltyCardProviderException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.LOYALTY_CARD_PROVIDER_EXCEPTION, message, cause)

    class InvalidLoyaltyCardException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_LOYALTY_CARD_EXCEPTION, message, cause)

    class LoyaltyCardNotFoundException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.LOYALTY_CARD_NOT_FOUND_EXCEPTION, message, cause)

    class AisleMoveException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.AISLE_MOVE_EXCEPTION, message, cause)

    class SignOutException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.SIGN_OUT_EXCEPTION, message, cause)

    class SignInException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.SIGN_IN_EXCEPTION, message, cause)

    class MissingCredentialException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.MISSING_CREDENTIAL_EXCEPTION, message, cause)

    class AuthException(
        exceptionCode: ExceptionCode, message: String? = null, cause: Throwable? = null
    ) : AisleronException(exceptionCode, message, cause)

    class NetworkException(
        exceptionCode: ExceptionCode, message: String? = null, cause: Throwable? = null
    ) : AisleronException(exceptionCode, message, cause)

    class NoSubscriptionException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.NO_SUBSCRIPTION_EXCEPTION, message, cause)

    enum class ExceptionCode {
        GENERIC_EXCEPTION,
        DELETE_DEFAULT_AISLE_EXCEPTION,
        DUPLICATE_PRODUCT_NAME_EXCEPTION,
        DUPLICATE_LOCATION_NAME_EXCEPTION,
        DUPLICATE_AISLE_NAME_EXCEPTION,
        INVALID_LOCATION_EXCEPTION,
        INVALID_DB_NAME_EXCEPTION,
        INVALID_DB_BACKUP_FILE_EXCEPTION,
        INVALID_DB_RESTORE_FILE_EXCEPTION,
        DUPLICATE_PRODUCT_EXCEPTION,
        DUPLICATE_LOCATION_EXCEPTION,
        SAMPLE_DATA_CREATION_EXCEPTION,
        LOYALTY_CARD_PROVIDER_EXCEPTION,
        INVALID_LOYALTY_CARD_EXCEPTION,
        LOYALTY_CARD_NOT_FOUND_EXCEPTION,
        INVALID_PRODUCT_EXCEPTION,
        AISLE_MOVE_EXCEPTION,
        SIGN_OUT_EXCEPTION,
        SIGN_IN_EXCEPTION,
        MISSING_CREDENTIAL_EXCEPTION,
        INVALID_CREDENTIAL_EXCEPTION,
        UNCONFIRMED_EMAIL_EXCEPTION,
        AUTH_EXCEPTION,
        NETWORK_EXCEPTION,
        NO_SUBSCRIPTION_EXCEPTION
    }
}

val Throwable?.exceptionCode: AisleronException.ExceptionCode
    get() = (this as? AisleronException)?.exceptionCode
        ?: AisleronException.ExceptionCode.GENERIC_EXCEPTION