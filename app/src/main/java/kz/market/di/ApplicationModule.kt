package kz.market.di

import android.content.Context
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
import kz.market.utils.update.UpdateManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

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

    @Provides
    @Singleton
    fun provideUpdateManager(@ApplicationContext context: Context): UpdateManager =
        UpdateManager(context, repoOwner = "dillybert", repoName = "MarketApp")
}