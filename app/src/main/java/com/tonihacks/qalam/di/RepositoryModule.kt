package com.tonihacks.qalam.di

import com.tonihacks.qalam.domain.repository.WordRepository
import com.tonihacks.qalam.data.repository.WordRepositoryImpl
import com.tonihacks.qalam.domain.repository.RootRepository
import com.tonihacks.qalam.data.repository.RootRepositoryImpl
import com.tonihacks.qalam.domain.repository.TextRepository
import com.tonihacks.qalam.data.repository.TextRepositoryImpl
import com.tonihacks.qalam.data.repository.TrainingRepositoryImpl
import com.tonihacks.qalam.domain.repository.TrainingRepository
import com.tonihacks.qalam.data.repository.AnalyticsRepositoryImpl
import com.tonihacks.qalam.domain.repository.AnalyticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWordRepository(impl: WordRepositoryImpl): WordRepository

    @Binds
    @Singleton
    abstract fun bindTextRepository(impl: TextRepositoryImpl): TextRepository

    @Binds
    @Singleton
    abstract fun bindRootRepository(impl: RootRepositoryImpl): RootRepository

    @Binds
    @Singleton
    abstract fun bindTrainingRepository(impl: TrainingRepositoryImpl): TrainingRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
}
