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

    class InvalidLocationException(message: String? = null, cause: Throwable? = null) :
        AisleronException(ExceptionCode.INVALID_LOCATION_EXCEPTION, message, cause)

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


    enum class ExceptionCode {
        GENERIC_EXCEPTION,
        DELETE_DEFAULT_AISLE_EXCEPTION,
        DUPLICATE_PRODUCT_NAME_EXCEPTION,
        DUPLICATE_LOCATION_NAME_EXCEPTION,
        INVALID_LOCATION_EXCEPTION,
        INVALID_DB_NAME_EXCEPTION,
        INVALID_DB_BACKUP_FILE_EXCEPTION,
        INVALID_DB_RESTORE_FILE_EXCEPTION,
        DUPLICATE_PRODUCT_EXCEPTION,
        DUPLICATE_LOCATION_EXCEPTION
    }
}