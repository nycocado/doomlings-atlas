package dev.nycocado.atlas.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

// The body of GitHub's latest Release, or null when the request fails. Each build type provides its own (UpdateModule).
fun interface LatestRelease {
    suspend fun fetch(): String?
}

private const val GOOGLE_PLAY = "com.android.vending"

// One per process, so the app asks once per cold start: not again when it comes back from the background or the
// activity is recreated.
@Singleton
class UpdateCheck
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val latestRelease: LatestRelease,
    ) {
        val update: StateFlow<AppUpdate?> =
            flow { emit(check()) }
                .stateIn(CoroutineScope(SupervisorJob() + Dispatchers.IO), SharingStarted.Lazily, null)

        // Google Play updates the app itself (ADR 0012).
        internal suspend fun check(): AppUpdate? {
            if (installer() == GOOGLE_PLAY) return null
            val body = latestRelease.fetch() ?: return null
            val installed =
                context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName
                    .orEmpty()
            return availableUpdate(body, installed)
        }

        private fun installer(): String? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
                } catch (_: PackageManager.NameNotFoundException) {
                    null
                }
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }
    }
