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

package com.aisleron.data.maintenance

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.sqlite.db.SimpleSQLiteQuery
import com.aisleron.data.AisleronDatabase
import com.aisleron.di.daoModule
import com.aisleron.di.databaseModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalModule
import com.aisleron.di.preferenceModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelModule
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

            val koinModules = listOf(
                daoModule,
                databaseModule,
                fragmentModule,
                generalModule,
                preferenceModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )

            unloadKoinModules(koinModules)
            loadKoinModules(koinModules)
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