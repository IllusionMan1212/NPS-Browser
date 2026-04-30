package com.illusionware.npsbrowser.data.download

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.illusionware.npsbrowser.data.network.ProgressListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val DOWNLOAD_STORE_NAME = "downloads"
private const val DOWNLOAD_TABLE_NAME = "download"

@Entity(tableName = DOWNLOAD_TABLE_NAME)
data class NPSPackageDownload(
    @PrimaryKey @ColumnInfo(name = "url")
    val url: String,
    val titleId: String,
    val name: String,
    val sha256: String?,
    val size: Long,
    var bytesDownloaded: Long,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "0")
    var statusValue: Int = State.PAUSED.value,
    var savedFileName: String? = null,
): ProgressListener {
    @Transient
    private val _statusFlow = MutableStateFlow(State.PAUSED)

    @Transient
    val statusFlow = _statusFlow.asStateFlow()
    var status: State
        get() = _statusFlow.value
        set(status) {
            statusValue = status.value
            _statusFlow.value = status
        }

    @Transient
    private val _progressFlow = MutableStateFlow(
        if (size > 0) bytesDownloaded.toFloat() / size.toFloat() else 0f
    )

    @Transient
    var progressFlow = _progressFlow.asStateFlow()
    private var progress: Float
        get() = _progressFlow.value
        set(value) {
            _progressFlow.value = value
        }

    @Transient
    private val _bytesReadFlow = MutableStateFlow(bytesDownloaded)

    @Transient
    var bytesDownloadedFlow = _bytesReadFlow.asStateFlow()
    private var bytesRead: Long
        get() = _bytesReadFlow.value
        set(value) {
            _bytesReadFlow.value = value
        }


    enum class State(val value: Int) {
        PAUSED(0),
        QUEUE(1),
        DOWNLOADING(2),
        DOWNLOADED(3),
        ERROR(4),

        ;

        companion object {
            fun fromValue(value: Int): State = entries.firstOrNull { it.value == value } ?: PAUSED
        }
    }

    fun restoreTransientState() {
        _statusFlow.value = State.fromValue(statusValue)
        _progressFlow.value = if (size > 0) {
            (bytesDownloaded.toFloat() / size.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        _bytesReadFlow.value = bytesDownloaded
    }

    override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
        progress = if (size > 0) {
            (bytesRead.toFloat() / size.toFloat()).coerceIn(0f, 1f)
        } else {
            0.0f
        }

        bytesDownloaded = bytesRead
        this.bytesRead = bytesRead
    }
}

@Dao
interface NPSPackageDownloadDao {
    @Query("SELECT * FROM download ORDER BY createdAt ASC")
    fun getAll(): Flow<List<NPSPackageDownload>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: NPSPackageDownload)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(download: NPSPackageDownload)

    @Query("DELETE FROM download WHERE url = :url")
    suspend fun delete(url: String)

    @Query("DELETE FROM download")
    suspend fun deleteAll()
}

@Database(entities = [NPSPackageDownload::class], version = 2)
abstract class DownloadStore : RoomDatabase() {
    abstract fun downloadDao(): NPSPackageDownloadDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE $DOWNLOAD_TABLE_NAME ADD COLUMN statusValue INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE $DOWNLOAD_TABLE_NAME ADD COLUMN savedFileName TEXT")
            }
        }

        @Volatile
        private var INSTANCE: DownloadStore? = null

        fun getDatabase(context: Context): DownloadStore {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DownloadStore::class.java,
                    DOWNLOAD_STORE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}