package com.illusionware.npsbrowser.data.download

import android.content.Context
import com.illusionware.npsbrowser.data.defaultDownloadDir
import com.illusionware.npsbrowser.data.network.GET
import com.illusionware.npsbrowser.data.network.NetworkHelper
import com.illusionware.npsbrowser.data.network.ProgressListener
import com.illusionware.npsbrowser.data.network.awaitSuccess
import com.illusionware.npsbrowser.data.network.newCachelessCallWithProgress
import com.illusionware.npsbrowser.util.system.saveTo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okio.appendingSink
import okio.buffer
import java.io.File
class Downloader(val context: Context, private val downloadManager: DownloadManager) {

    private val invalidFilenameChars = Regex("[\\\\/:*?\"<>|]")
    private val whitespaceRegex = Regex("\\s+")

    private val store = DownloadStore.getDatabase(context)
    private val networkHelper = NetworkHelper(context)
    private val dao = store.downloadDao()

    private val _queueState = MutableStateFlow<List<NPSPackageDownload>>(emptyList())
    val queueState = _queueState.asStateFlow()

    private val queueMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var workerJob: Job? = null
    private var currentDownloadJob: Job? = null

    @Volatile
    private var currentDownloadUrl: String? = null

    private val notifier by lazy { DownloadNotifier(context) }

    val isRunning: Boolean
        get() = workerJob?.isActive ?: false

    @Volatile
    var isPaused: Boolean = false

    init {
        scope.launch {
            val storeItems = dao.getAll().first()
            addAllToQueue(storeItems)
        }
    }

    fun shouldConfirmOverwrite(url: String): Boolean {
        val existing = queueState.value.find { it.url == url } ?: return false
        return existing.status == NPSPackageDownload.State.DOWNLOADED && downloadedFileFor(existing).exists()
    }

    fun enqueue(download: NPSPackageDownload, autoStart: Boolean, overwriteExisting: Boolean) {
        scope.launch {
            val shouldInsert = queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                val existing = queue.find { it.url == download.url }
                if (existing != null && existing.status == NPSPackageDownload.State.DOWNLOADED && downloadedFileFor(existing).exists()) {
                    if (!overwriteExisting) {
                        _queueState.value = queue
                        return@withLock false
                    }

                    downloadedFileFor(existing).delete()
                }

                queue.removeAll { it.url == download.url }
                download.savedFileName = null
                download.bytesDownloaded = 0L
                download.status = NPSPackageDownload.State.QUEUE
                download.restoreTransientState()
                queue.add(download)
                _queueState.value = queue
                true
            }

            if (!shouldInsert) {
                return@launch
            }

            dao.insert(download)

            if (autoStart) {
                isPaused = false
                launchWorkerIfNeeded()
                if (!DownloadService.isRunning(context)) {
                    DownloadService.start(context, downloadManager)
                }
            }
        }
    }

    fun start(): Boolean {
        if (isRunning) return true

        val hasPending = queueState.value.any {
            it.status == NPSPackageDownload.State.QUEUE ||
                it.status == NPSPackageDownload.State.PAUSED ||
                it.status == NPSPackageDownload.State.ERROR
        }
        if (!hasPending) return false

        scope.launch {
            queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                queue.forEach { download ->
                    if (download.status == NPSPackageDownload.State.PAUSED || download.status == NPSPackageDownload.State.ERROR) {
                        download.status = NPSPackageDownload.State.QUEUE
                    }
                }
                _queueState.value = queue
            }
            isPaused = false
            launchWorkerIfNeeded()
        }

        return true
    }

    fun pauseAll() {
        scope.launch {
            queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                queue.forEach { download ->
                    if (download.status == NPSPackageDownload.State.DOWNLOADING || download.status == NPSPackageDownload.State.QUEUE) {
                        download.status = NPSPackageDownload.State.PAUSED
                    }
                }
                _queueState.value = queue
            }

            currentDownloadJob?.cancel()
            workerJob?.cancel()
            currentDownloadJob = null
            workerJob = null
            isPaused = true
            notifier.onPaused()
        }
    }

    fun stop(reason: String? = null) {
        scope.launch {
            queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                queue.forEach { download ->
                    if (download.status == NPSPackageDownload.State.DOWNLOADING || download.status == NPSPackageDownload.State.QUEUE) {
                        download.status = NPSPackageDownload.State.PAUSED
                    }
                }
                _queueState.value = queue
            }

            currentDownloadJob?.cancel()
            workerJob?.cancel()
            currentDownloadJob = null
            workerJob = null
            isPaused = queueState.value.isNotEmpty()
            if (reason != null) {
                notifier.onPaused(reason)
            } else if (isPaused) {
                notifier.onPaused()
            } else {
                notifier.onComplete()
            }

            if (reason == null && DownloadService.isRunning.value) {
                DownloadService.stop(context)
            }
        }
    }

    fun pause(url: String) {
        scope.launch {
            var pauseCurrent = false
            queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                queue.forEach { download ->
                    if (download.url == url) {
                        if (download.status == NPSPackageDownload.State.DOWNLOADING || download.status == NPSPackageDownload.State.QUEUE) {
                            download.status = NPSPackageDownload.State.PAUSED
                        }
                        pauseCurrent = currentDownloadUrl == url
                    }
                }
                _queueState.value = queue
            }

            if (pauseCurrent) {
                currentDownloadJob?.cancel()
            }

            if (areAllPaused()) {
                workerJob?.cancel()
                workerJob = null
                isPaused = true
                notifier.onPaused()
                if (DownloadService.isRunning.value) {
                    DownloadService.stop(context)
                }
            }
        }
    }

    fun resume(url: String) {
        scope.launch {
            if (queueState.value.isEmpty()) return@launch

            queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                val index = queue.indexOfFirst { it.url == url }
                if (index == -1) return@withLock

                val target = queue.removeAt(index)
                if (target.status == NPSPackageDownload.State.DOWNLOADED && downloadedFileFor(target).exists()) {
                    queue.add(index, target)
                    _queueState.value = queue
                    return@withLock
                }
                target.status = NPSPackageDownload.State.QUEUE
                // Prioritize explicit resume action so the tapped item starts next.
                queue.add(0, target)
                _queueState.value = queue
            }

            isPaused = false
            launchWorkerIfNeeded()
            if (!DownloadService.isRunning(context)) {
                DownloadService.start(context, downloadManager)
            }
        }
    }

    private fun launchWorkerIfNeeded() {
        if (workerJob?.isActive == true) return

        workerJob = scope.launch {
            while (true) {
                val nextDownload = queueMutex.withLock {
                    _queueState.value.firstOrNull { it.status == NPSPackageDownload.State.QUEUE }
                } ?: break

                currentDownloadUrl = nextDownload.url

                val job = scope.launch {
                    processDownload(nextDownload)
                }
                currentDownloadJob = job

                job.join()
                currentDownloadJob = null
                currentDownloadUrl = null
            }

            if (queueState.value.none { it.status == NPSPackageDownload.State.QUEUE || it.status == NPSPackageDownload.State.DOWNLOADING }) {
                notifier.onComplete()
                isPaused = queueState.value.isNotEmpty() && queueState.value.all { it.status == NPSPackageDownload.State.PAUSED }
                if (DownloadService.isRunning.value) {
                    DownloadService.stop(context)
                }
            }

            workerJob = null
        }
    }

    private suspend fun processDownload(download: NPSPackageDownload) {
        try {
            queueMutex.withLock {
                download.status = NPSPackageDownload.State.DOWNLOADING
                _queueState.value = _queueState.value.toList()
            }

            coroutineScope {
                downloadPackage(download, this)
            }

            queueMutex.withLock {
                download.bytesDownloaded = download.size
                download.restoreTransientState()
                download.status = NPSPackageDownload.State.DOWNLOADED
                _queueState.value = _queueState.value.toList()
            }

            dao.update(download)
        } catch (_: CancellationException) {
            dao.update(download)
        } catch (error: Throwable) {
            queueMutex.withLock {
                download.status = NPSPackageDownload.State.ERROR
                _queueState.value = _queueState.value.toList()
            }
            dao.update(download)
            notifier.onError(error.message ?: "Download failed")
        }
    }

    private suspend fun downloadPackage(download: NPSPackageDownload, scope: CoroutineScope) {
        val file = prepareTargetFile(download)
        val resumedBytes = if (file.exists()) file.length() else 0L

        if (download.bytesDownloaded != resumedBytes) {
            download.bytesDownloaded = resumedBytes
            download.restoreTransientState()
            dao.update(download)
        }

        val startBytes = download.bytesDownloaded
        var lastPersist = 0L

        val listener = object : ProgressListener {
            override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
                download.update(bytesRead, contentLength, done)

                val totalSize = if (contentLength > 0) {
                    contentLength + startBytes
                } else {
                    download.size
                }
                notifier.onProgressChange(totalSize, bytesRead)

                val now = System.currentTimeMillis()
                if (done || now - lastPersist >= 1000L) {
                    lastPersist = now
                    this@Downloader.scope.launch {
                        dao.update(download)
                    }
                }
            }
        }

        val res = networkHelper.client.newCachelessCallWithProgress(
            GET(download.url, Headers.headersOf("Range", "bytes=${download.bytesDownloaded}-")),
            listener,
            download.bytesDownloaded
        ).awaitSuccess()

        file.parentFile?.mkdirs()
        val fileBufferedSink = file.appendingSink().buffer()

        try {
            res.body!!.source().saveTo(
                fileBufferedSink,
                res.body!!.contentLength(),
                download.bytesDownloaded,
                scope
            )
            dao.update(download)
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            throw e
        } finally {
            withContext(Dispatchers.IO) {
                fileBufferedSink.close()
            }
            res.close()
        }
    }

    private fun areAllPaused(): Boolean {
        val unfinished = queueState.value.filter { it.status != NPSPackageDownload.State.DOWNLOADED }
        return unfinished.isNotEmpty() && unfinished.all { it.status == NPSPackageDownload.State.PAUSED }
    }

    private fun addAllToQueue(downloads: List<NPSPackageDownload>) {
        _queueState.update {
            downloads.forEach { download ->
                download.restoreTransientState()
                when (download.status) {
                    NPSPackageDownload.State.DOWNLOADING,
                    NPSPackageDownload.State.QUEUE,
                    -> download.status = NPSPackageDownload.State.PAUSED

                    NPSPackageDownload.State.DOWNLOADED -> {
                        if (!downloadedFileFor(download).exists()) {
                            download.savedFileName = null
                            download.bytesDownloaded = 0L
                            download.restoreTransientState()
                            download.status = NPSPackageDownload.State.ERROR
                        }
                    }

                    else -> Unit
                }
            }
            downloads
        }
    }

    fun removeDownload(url: String) {
        scope.launch {
            val removed = queueMutex.withLock {
                val queue = _queueState.value.toMutableList()
                val item = queue.find { it.url == url } ?: return@withLock null
                queue.remove(item)
                _queueState.value = queue
                item
            } ?: return@launch

            if (currentDownloadUrl == removed.url) {
                currentDownloadJob?.cancel()
            }

            dao.delete(url)

            val file = downloadedFileFor(removed)
            file.delete()

            if (queueState.value.isEmpty()) {
                workerJob?.cancel()
                workerJob = null
                currentDownloadJob = null
                notifier.onComplete()
                if (DownloadService.isRunning.value) {
                    DownloadService.stop(context)
                }
            }
        }
    }

    private fun prepareTargetFile(download: NPSPackageDownload): File {
        val existingFileName = download.savedFileName
        if (existingFileName != null) {
            return File(defaultDownloadDir, existingFileName)
        }

        val candidate = File(defaultDownloadDir, download.defaultFileName())

        download.savedFileName = candidate.name
        return candidate
    }

    private fun downloadedFileFor(download: NPSPackageDownload): File {
        return File(defaultDownloadDir, download.savedFileName ?: download.defaultFileName())
    }

    private fun NPSPackageDownload.defaultFileName(): String {
        val normalizedName = name
            .trim()
            .replace(invalidFilenameChars, "_")
            .replace(whitespaceRegex, " ")
            .replace(Regex("\\.pkg$", RegexOption.IGNORE_CASE), "")
            .trimEnd('.', ' ')
            .ifBlank { titleId }

        return "[$titleId] $normalizedName.pkg"
    }
}
