package com.aisleron.data.maintenance

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.sqlite.db.SimpleSQLiteQuery
import com.aisleron.data.AisleronDatabase
import com.aisleron.di.appModule
import com.aisleron.domain.backup.DatabaseMaintenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.java.KoinJavaComponent.getKoin
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class DatabaseMaintenanceImpl(
    private val database: AisleronDatabase,
    private val context: Context
) : DatabaseMaintenance {

    companion object {
        private const val BUFFER_SIZE = 8192
    }

    override fun getDatabaseName() = database.openHelper.databaseName

    override suspend fun backupDatabase(backupFolderUri: Uri, backupFileName: String) {
        val backupFolder = DocumentFile.fromTreeUri(context, backupFolderUri)
        val backupFile = backupFolder?.createFile("*/*", backupFileName)
        val backupStream: OutputStream? = backupFile?.let {
            context.contentResolver.openOutputStream(it.uri)
        }

        createCheckpoint()
        withContext(Dispatchers.IO) {
            val databaseStream = FileInputStream(database.openHelper.writableDatabase.path)
            backupStream?.let { copy(databaseStream, it) }

            databaseStream.close()
            backupStream?.flush()
            backupStream?.close()
        }
    }

    override suspend fun restoreDatabase(restoreFileUri: Uri) {
        val restoreStream = context.contentResolver.openInputStream(restoreFileUri)
        withContext(Dispatchers.IO) {
            val databaseStream = FileOutputStream(database.openHelper.writableDatabase.path)
            database.close()

            restoreStream?.let { copy(it, databaseStream) }

            restoreStream?.close()
            databaseStream.flush()
            databaseStream.close()
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