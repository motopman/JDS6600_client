package com.jds6600.sequencer.di

import android.content.Context
import androidx.room.Room
import com.jds6600.sequencer.data.local.database.SequenceDao
import com.jds6600.sequencer.data.local.database.SequenceDatabase
import com.jds6600.sequencer.data.repository.ConnectionRepositoryImpl
import com.jds6600.sequencer.data.repository.SequenceRepositoryImpl
import com.jds6600.sequencer.data.remote.websocket.WebSocketManager
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import com.jds6600.sequencer.domain.repository.ISequenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SequenceDatabase =
        Room.databaseBuilder(ctx, SequenceDatabase::class.java, "sequence.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSequenceDao(db: SequenceDatabase): SequenceDao = db.sequenceDao()

    @Provides
    @Singleton
    fun provideSequenceRepository(impl: SequenceRepositoryImpl): ISequenceRepository = impl

    @Provides
    @Singleton
    fun provideConnectionRepository(impl: ConnectionRepositoryImpl): IConnectionRepository = impl
}
