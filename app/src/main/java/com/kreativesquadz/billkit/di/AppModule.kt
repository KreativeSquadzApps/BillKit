package com.kreativesquadz.billkit.di

import android.content.Context
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
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
    fun providedb(@ApplicationContext appContext: Context): AppDatabase {
        return AppDatabase.getInstance(appContext)!!
    }

    @Provides
    fun provideCustomerRepository(@ApplicationContext appContext: Context,providedb: AppDatabase): CustomerManagRepository{
        return CustomerManagRepository(context = appContext,providedb)
    }

    @Provides
    fun provideBillHistoryRepository(providedb: AppDatabase): BillHistoryRepository {
        return BillHistoryRepository(providedb)
    }

    @Provides
    fun provideSettingsRepository(providedb: AppDatabase): SettingsRepository {
        return SettingsRepository(providedb)
    }

}