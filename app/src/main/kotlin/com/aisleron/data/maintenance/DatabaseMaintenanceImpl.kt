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

    companion object {
        private const val BUFFER_SIZE = 8192
    }

    private val _coroutineDispatcher = coroutineDispatcher ?: Dispatchers.IO


    override fun getDatabaseName() = database.openHelper.databaseName

    override suspend fun backupDatabase(backupFolderUri: URI, backupFileName: String) {
        val uri = Uri.parse(backupFolderUri.toString())
        val backupFolder = DocumentFile.fromTreeUri(context, uri)
        val backupFile = backupFolder?.createFile("application/vnd.sqlite3", backupFileName)
        val backupStream = backupFile?.let {
            context.contentResolver.openOutputStream(it.uri)
        }

        if (backupStream == null)
            throw AisleronException.InvalidDbBackupFileException("Invalid database backup file/location")

        withContext(_coroutineDispatcher) {
            createCheckpoint()
            val databaseStream = FileInputStream(database.openHelper.writableDatabase.path)
            try {
                copy(databaseStream, backupStream)
            } finally {
                databaseStream.close()
                backupStream.flush()
                backupStream.close()
            }
        }
    }

    override suspend fun restoreDatabase(restoreFileUri: URI) {
        val uri = Uri.parse(restoreFileUri.toString())
        val restoreStream = context.contentResolver.openInputStream(uri)
            ?: throw AisleronException.InvalidDbRestoreFileException("Invalid database backup file/location")

        withContext(_coroutineDispatcher) {
            val databaseStream = FileOutputStream(database.openHelper.writableDatabase.path)
            database.close()
            try {
                copy(restoreStream, databaseStream)
            } finally {
                restoreStream.close()
                databaseStream.flush()
                databaseStream.close()
            }
            unloadKoinModules(appModule)
            loadKoinModules(appModule)
            getKoin().get<AisleronDatabase>()
        }
    }

    private suspend fun createCheckpoint() {
        database.maintenanceDao().checkpoint((SimpleSQLiteQuery("pragma wal_checkpoint(full)")))
    }

    private fun copy(source: InputStream, sink: OutputStream): Long {
        var totalBytes = 0L
        val buffer = ByteArray(BUFFER_SIZE)
        var readChunk: Int
        while ((source.read(buffer).also { readChunk = it }) > 0) {
            sink.write(buffer, 0, readChunk)
            totalBytes += readChunk.toLong()
        }
        return totalBytes
    }
}