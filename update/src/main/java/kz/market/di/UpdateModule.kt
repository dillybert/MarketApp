package kz.market.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kz.market.data.repository.UpdateRepositoryImpl
import kz.market.domain.repository.UpdateRepository

@Module
@InstallIn(SingletonComponent::class)
object UpdateModule {

    @Provides
    fun provideUpdateRepository(
        updateRepository: UpdateRepositoryImpl
    ): UpdateRepository = updateRepository

}