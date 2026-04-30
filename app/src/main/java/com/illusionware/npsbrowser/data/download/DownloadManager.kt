package com.illusionware.npsbrowser.data.download

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

class DownloadManager(private val context: Context) {
    private val downloader = Downloader(context, this)

    val queueState
        get() = downloader.queueState

    fun downloaderStart() = downloader.start()
    fun downloaderStop(reason: String? = null) = downloader.stop(reason)

    val isDownloaderRunning
        get() = DownloadService.isRunning
    val isDownloaderPaused
        get() = downloader.isPaused

    fun startDownloads() {
        if (DownloadService.isRunning(context)) {
            return
        }
        DownloadService.start(context, this)
    }

    fun pauseDownload(url: String) = downloader.pause(url)
    fun resumeDownload(url: String) = downloader.resume(url)

    fun pauseDownloads() {
        downloader.pauseAll()
        downloader.stop()
    }

    fun shouldConfirmOverwrite(url: String): Boolean {
        return downloader.shouldConfirmOverwrite(url)
    }

    fun download(package_: NPSPackageDownload, autoStart: Boolean = true, overwriteExisting: Boolean = false) {
        downloader.enqueue(package_, autoStart, overwriteExisting)
    }

    fun cancelDownload(url: String) {
        val wasRunning = downloader.isRunning
//        if (wasRunning) {
//            downloader.pauseAll()
//        }

        downloader.removeDownload(url)

        if (wasRunning && queueState.value.isEmpty()) {
            downloader.stop()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun statusFlow(): Flow<NPSPackageDownload> = queueState
        .flatMapLatest { downloads ->
            downloads
                .map { download ->
                    download.statusFlow.drop(1).map { download }
                }
                .merge()
        }
        .onStart {
            emitAll(queueState.value.filter { download -> download.status == NPSPackageDownload.State.DOWNLOADING }.asFlow())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun progressFlow(): Flow<NPSPackageDownload> = queueState
        .flatMapLatest { downloads ->
            downloads
                .map { download ->
                    download.progressFlow.drop(1).map { download }
                }
                .merge()
        }
        .onStart {
            emitAll(queueState.value.filter { download -> download.status == NPSPackageDownload.State.DOWNLOADING }.asFlow())
        }
}