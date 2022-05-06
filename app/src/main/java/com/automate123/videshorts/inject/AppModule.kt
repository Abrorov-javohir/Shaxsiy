package com.automate123.videshorts.inject

import android.content.Context
import com.automate123.videshorts.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideFile(@ApplicationContext context: Context): File {
        with(context) {
            return if (BuildConfig.DEBUG) externalCacheDir!! else cacheDir
        }
    }
}