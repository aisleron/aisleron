package com.aisleron.data.maintenance

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.sqlite.db.SimpleSQLiteQuery
import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.backup.DatabaseMaintenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
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

    override suspend fun backupDatabase(backupUri: Uri, backupFileName: String) {
        val backupFolder = DocumentFile.fromTreeUri(context, backupUri)
        val backupFile = backupFolder?.createFile("*/*", backupFileName)
        val backupStream: OutputStream? = backupFile?.let {
            context.contentResolver.openOutputStream(it.uri)
        }

        createCheckpoint()
        withContext(Dispatchers.IO) {
            val databaseStream = FileInputStream(database.openHelper.writableDatabase.path)
            backupStream?.let { copy(databaseStream, it) }

            backupStream?.flush()
            backupStream?.close()
            databaseStream.close()
        }
    }

    override suspend fun restoreDatabase(backupUri: String) {
        File(database.openHelper.writableDatabase.path)
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