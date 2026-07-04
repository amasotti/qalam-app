package com.tonihacks.qalam.di

import com.tonihacks.qalam.domain.repository.WordRepository
import com.tonihacks.qalam.data.repository.WordRepositoryImpl
import com.tonihacks.qalam.domain.repository.RootRepository
import com.tonihacks.qalam.data.repository.RootRepositoryImpl
import com.tonihacks.qalam.domain.repository.TextRepository
import com.tonihacks.qalam.data.repository.TextRepositoryImpl
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
}
