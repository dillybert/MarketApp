package kz.market.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kz.market.data.receiver.InstallStateHolder

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InstallReceiverEntryPoint {
    fun installStateHolder(): InstallStateHolder
}