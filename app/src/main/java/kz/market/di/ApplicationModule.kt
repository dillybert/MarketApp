package kz.market.di

import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kz.market.data.repository.ProductRepositoryImpl
import kz.market.data.repository.SupplierSuggestionRepositoryImpl
import kz.market.domain.repository.ProductRepository
import kz.market.domain.repository.SupplierSuggestionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository = impl

    @Provides
    @Singleton
    fun provideSupplierSuggestionRepository(
        impl: SupplierSuggestionRepositoryImpl
    ): SupplierSuggestionRepository = impl
}