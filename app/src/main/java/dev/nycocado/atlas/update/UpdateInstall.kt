package dev.nycocado.atlas.update

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dev.nycocado.atlas.R
import java.io.File

// Runs while the activity is started, so a download that finishes in the background opens the installer when the
// player comes back: Android doesn't let a backgrounded app start an activity.
@Composable
fun InstallUpdate(
    download: DownloadState,
    onAwaitPermission: (File) -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    // The setting returns no result: whether the player granted it is read again on return.
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            (download as? DownloadState.AwaitingPermission)?.let {
                if (context.packageManager.canRequestPackageInstalls()) openInstaller(context, it.apk)
            }
            onDone()
        }
    LaunchedEffect(download) {
        when (download) {
            is DownloadState.Ready -> {
                if (context.packageManager.canRequestPackageInstalls()) {
                    openInstaller(context, download.apk)
                    onDone()
                } else {
                    onAwaitPermission(download.apk)
                    permission.launch(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri()),
                    )
                }
            }

            DownloadState.Corrupt -> {
                Toast.makeText(context, R.string.update_corrupt, Toast.LENGTH_LONG).show()
                onDone()
            }

            DownloadState.Idle, DownloadState.Downloading, is DownloadState.AwaitingPermission -> {
                Unit
            }
        }
    }
}

// The system installer, where the player confirms; Android itself refuses an APK signed with another key.
private fun openInstaller(
    context: Context,
    apk: File,
) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
    context.startActivity(
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, APK_TYPE)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
    )
}
