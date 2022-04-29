package com.automate123.videshorts.inject

import android.content.Context
import androidx.core.content.ContextCompat
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideMainExecutor(@ApplicationContext context: Context): Executor {
        return ContextCompat.getMainExecutor(context)
    }
}