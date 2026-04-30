package com.illusionware.npsbrowser.data.download

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.map

class DownloadRepository(private val downloadDAO: NPSPackageDownloadDao) {
    val allDownloads = downloadDAO.getAll()

    @WorkerThread
    suspend fun insert(download: NPSPackageDownload) {
        downloadDAO.insert(download)
    }

    @WorkerThread
    suspend fun delete(url: String) {
        downloadDAO.delete(url)
    }

    @WorkerThread
    suspend fun deleteAll() {
        downloadDAO.deleteAll()
    }
}