package com.aisleron.data.maintenance

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.sqlite.db.SimpleSQLiteQuery
import com.aisleron.data.AisleronDatabase
import com.aisleron.di.appModule
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.base.AisleronException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.java.KoinJavaComponent.getKoin
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

class DatabaseMaintenanceImpl(
    private val database: AisleronDatabase,
    private val context: Context,
    coroutineDispatcher: CoroutineDispatcher? = null
) : DatabaseMaintenance {

    private val _coroutineDispatcher = coroutineDispatcher ?: Dispatchers.IO

    override fun getDatabaseName() = database.openHelper.databaseName

    override suspend fun backupDatabase(backupFolderUri: URI, backupFileName: String) {
        val uri = Uri.parse(backupFolderUri.toString())
        val backupFolder = DocumentFile.fromTreeUri(context, uri)
        val backupFile = backupFolder?.createFile("application/vnd.sqlite3", backupFileName)
        val outputStream = backupFile?.let {
            context.contentResolver.openOutputStream(it.uri)
        }

        if (outputStream == null)
            throw AisleronException.InvalidDbBackupFileException("Invalid database backup file/location")

        withContext(_coroutineDispatcher) {
            createCheckpoint()
            val inputStream = FileInputStream(database.openHelper.writableDatabase.path)
            copyAndClose(inputStream, outputStream)
        }
    }

    override suspend fun restoreDatabase(restoreFileUri: URI) {
        val uri = Uri.parse(restoreFileUri.toString())
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw AisleronException.InvalidDbRestoreFileException("Invalid database backup file/location")

        withContext(_coroutineDispatcher) {
            val dbPath = database.openHelper.writableDatabase.path
            database.close()
            val outputStream = FileOutputStream(dbPath)
            copyAndClose(inputStream, outputStream)
            unloadKoinModules(appModule)
            loadKoinModules(appModule)
            getKoin().get<AisleronDatabase>()
        }
    }

    private suspend fun createCheckpoint() {
        database.maintenanceDao().checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
    }

    private fun copyAndClose(inputStream: InputStream, outputStream: OutputStream): Long {
        try {
            return inputStream.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
        } finally {
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
    }
}