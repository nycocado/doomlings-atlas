package dev.nycocado.atlas.update

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val LATEST_RELEASE = "https://api.github.com/repos/nycocado/doomlings-atlas/releases/latest"

// The app's only network request (ADR 0012), through the platform's HttpURLConnection rather than a network library.
private suspend fun fetchLatestRelease(): String? =
    withContext(Dispatchers.IO) {
        try {
            val connection = URL(LATEST_RELEASE).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        } catch (_: IOException) {
            null
        }
    }

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {
    @Provides
    fun latestRelease(): LatestRelease = LatestRelease(::fetchLatestRelease)
}
