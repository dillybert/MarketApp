package kz.market.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kz.market.service.utils.UpdateEventBus

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InstallReceiverEntryPoint {
    fun updateEventBus(): UpdateEventBus
}