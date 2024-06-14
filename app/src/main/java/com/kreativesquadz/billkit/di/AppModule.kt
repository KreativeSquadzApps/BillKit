package com.kreativesquadz.billkit.di

import android.content.Context
import com.kreativesquadz.billkit.Database.AppDatabase
import com.kreativesquadz.billkit.repository.BillHistoryRepository
import com.kreativesquadz.billkit.repository.CreditNoteRepository
import com.kreativesquadz.billkit.repository.CustomerManagRepository
import com.kreativesquadz.billkit.repository.InventoryRepository
import com.kreativesquadz.billkit.repository.SettingsRepository
import com.kreativesquadz.billkit.repository.UserSettingRepository
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
    fun provideCustomerRepository(providedb: AppDatabase): CustomerManagRepository{
        return CustomerManagRepository(providedb)
    }

    @Provides
    fun provideBillHistoryRepository(providedb: AppDatabase): BillHistoryRepository {
        return BillHistoryRepository(providedb)
    }

    @Provides
    fun provideSettingsRepository(providedb: AppDatabase): SettingsRepository {
        return SettingsRepository(providedb)
    }

    @Provides
    fun provideInventoryRepository(providedb: AppDatabase): InventoryRepository {
        return InventoryRepository(providedb)

    }

    @Provides
    fun provideUserRepository(providedb: AppDatabase) : UserSettingRepository {
        return UserSettingRepository(providedb)
    }

    @Provides
    fun  provideCreditNoteReppoository(providedb: AppDatabase): CreditNoteRepository{
        return CreditNoteRepository(providedb)
    }

}