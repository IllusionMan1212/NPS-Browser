package com.illusionware.npsbrowser.data.download

import android.app.Notification
import android.app.Service
import android.content.pm.ServiceInfo
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.illusionware.npsbrowser.R
import com.illusionware.npsbrowser.data.notification.Notifications
import com.illusionware.npsbrowser.util.system.acquireWakeLock
import com.illusionware.npsbrowser.util.system.isOnline
import com.illusionware.npsbrowser.util.system.isServiceRunning
import com.illusionware.npsbrowser.util.system.notificationBuilder
import com.illusionware.npsbrowser.util.system.toast
import com.illusionware.npsbrowser.util.system.withUIContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.beryukhov.reactivenetwork.ReactiveNetwork

class DownloadService : Service() {
    companion object {
        private lateinit var _downloadManager: DownloadManager
        private val _isRunning = MutableStateFlow(false)
        val isRunning = _isRunning.asStateFlow()

        fun start(context: Context, downloadManager: DownloadManager) {
            Log.d("DOWNLOAD", "Starting download service")
            val intent = Intent(context, DownloadService::class.java)
            _downloadManager = downloadManager
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DownloadService::class.java))
        }

        fun isRunning(context: Context): Boolean {
            return context.isServiceRunning(DownloadService::class.java)
        }
    }

    private lateinit var wakeLock: WakeLock

    private lateinit var scope: CoroutineScope

    override fun onCreate() {
        Log.d("DOWNLOAD", "In onCreate, acquring wakelock")
        super.onCreate()
        scope = CoroutineScope(Dispatchers.IO)
        ServiceCompat.startForeground(
            this,
            Notifications.ID_DOWNLOADER_GROUP,
            getPlaceholderNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
        wakeLock = acquireWakeLock(javaClass.name)
        _isRunning.value = true
        listenNetworkChanges()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        _isRunning.value = false
        _downloadManager.downloaderStop()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun downloaderStop(string: String) {
        _downloadManager.downloaderStop(string)
    }

    private fun listenNetworkChanges() {
        Log.d("DOWNLOAD", "Listening to network changes")
        ReactiveNetwork()
            .observeNetworkConnectivity(applicationContext)
            .onEach {
                withUIContext {
                    if (isOnline()) {
                        Log.d("DOWNLOAD", "We're online so we start the downloader")
                        val started = _downloadManager.downloaderStart()
                        if (!started) stopSelf()
                    } else {
                        downloaderStop("Not connected to the internet")
                    }
                }
            }
            .catch { error ->
                withUIContext {
//                    logcat(LogPriority.ERROR, error)
                    toast("Download failed: $error")
                    stopSelf()
                }
            }
            .launchIn(scope)
    }

    private fun getPlaceholderNotification(): Notification {
        return notificationBuilder(Notifications.CHANNEL_DOWNLOADER) {
            setSmallIcon(R.drawable.baseline_cloud_download_24)
            setCategory(androidx.core.app.NotificationCompat.CATEGORY_SERVICE)
            setOnlyAlertOnce(true)
            setOngoing(true)
            setSilent(true)
            setContentTitle("Preparing downloads")
            setContentText("Starting downloader service")
        }.build()
    }
}