package dev.nycocado.atlas.update

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Debug builds never ask GitHub: their version is 0.0.0, so every Release would look newer.
@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {
    @Provides
    fun latestRelease(): LatestRelease = LatestRelease { null }
}
