package dev.nycocado.atlas.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed interface DownloadState {
    data object Idle : DownloadState

    data object Downloading : DownloadState

    // Checked against the Release's digest, waiting for the activity to hand it to the system installer.
    data class Ready(
        val apk: File,
    ) : DownloadState

    // Ready, while the player is in the system setting that lets the app install unknown apps.
    data class AwaitingPermission(
        val apk: File,
    ) : DownloadState

    // The file didn't match the digest and was deleted.
    data object Corrupt : DownloadState
}

// One per process, so a download started on one screen finishes on any other. Progress shows in the system
// notification. A download the process doesn't live to see is dropped: the next cold start checks again (#110).
@Singleton
class UpdateDownload
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val mutableState = MutableStateFlow<DownloadState>(DownloadState.Idle)
        val state: StateFlow<DownloadState> = mutableState.asStateFlow()

        private val downloads = context.getSystemService(DownloadManager::class.java)
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun start(update: AppUpdate) {
            if (mutableState.value != DownloadState.Idle) return
            val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return
            // DownloadManager renames rather than overwrites, so a file left by an earlier attempt goes first.
            val apk = File(folder, "doomlings-atlas-v${update.version}.apk").apply { delete() }
            mutableState.value = DownloadState.Downloading
            val id =
                downloads.enqueue(
                    DownloadManager
                        .Request(update.apkUrl.toUri())
                        .setTitle(apk.name)
                        .setMimeType(APK_TYPE)
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationUri(apk.toUri()),
                )
            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        receiverContext: Context,
                        intent: Intent,
                    ) {
                        if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) != id) return
                        context.unregisterReceiver(this)
                        scope.launch { mutableState.value = finished(id, apk, update.sha256) }
                    }
                }
            // Exported: the broadcast comes from the download provider, another app. Anyone could send it, but it
            // only says which download to look at, and its status comes from DownloadManager itself.
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED,
            )
        }

        // A failed download says so in its notification, so the state just goes back to Idle.
        private fun finished(
            id: Long,
            apk: File,
            sha256: String,
        ): DownloadState {
            val successful =
                downloads.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
                    cursor.moveToFirst() &&
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)) ==
                        DownloadManager.STATUS_SUCCESSFUL
                }
            return when {
                !successful || !apk.isFile -> DownloadState.Idle.also { apk.delete() }
                keepIfIntact(apk, sha256) -> DownloadState.Ready(apk)
                else -> DownloadState.Corrupt
            }
        }

        fun awaitPermission(apk: File) {
            mutableState.value = DownloadState.AwaitingPermission(apk)
        }

        // The installer opened, the player declined the permission, or the corrupt download was reported.
        fun done() {
            mutableState.value = DownloadState.Idle
        }
    }

internal const val APK_TYPE = "application/vnd.android.package-archive"
