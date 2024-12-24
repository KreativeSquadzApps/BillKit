package com.kreativesquadz.hisabkitab.di

import android.content.Context
import androidx.work.WorkManager
import com.kreativesquadz.hisabkitab.Database.AppDatabase
import com.kreativesquadz.hisabkitab.bluetooth.BluetoothService
import com.kreativesquadz.hisabkitab.repository.LoginRepository
import com.kreativesquadz.hisabkitab.repository.BillHistoryRepository
import com.kreativesquadz.hisabkitab.repository.BillSettingsRepository
import com.kreativesquadz.hisabkitab.repository.BluetoothRepository
import com.kreativesquadz.hisabkitab.repository.CreditNoteRepository
import com.kreativesquadz.hisabkitab.repository.CreditRepository
import com.kreativesquadz.hisabkitab.repository.CustomerManagRepository
import com.kreativesquadz.hisabkitab.repository.GstTaxRepository
import com.kreativesquadz.hisabkitab.repository.InventoryRepository
import com.kreativesquadz.hisabkitab.repository.SavedOrderRepository
import com.kreativesquadz.hisabkitab.repository.SettingsRepository
import com.kreativesquadz.hisabkitab.repository.StaffManagRepository
import com.kreativesquadz.hisabkitab.repository.UserSettingRepository
import com.kreativesquadz.hisabkitab.utils.PreferencesHelper
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
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideBluetoothService(@ApplicationContext appContext: Context , userSettingRepository: UserSettingRepository ): BluetoothService {
        return BluetoothService(appContext,userSettingRepository)
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
    fun provideBillSettingsRepository(providedb: AppDatabase): BillSettingsRepository {
        return BillSettingsRepository(providedb)
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
    fun  provideCreditNoteRepository(providedb: AppDatabase): CreditNoteRepository{
        return CreditNoteRepository(providedb)
    }

    @Provides
    fun provideBluetoothRepository(bluetoothService: BluetoothService): BluetoothRepository {
        return BluetoothRepository(bluetoothService)
    }


    @Provides
    fun provideStaffRepository(providedb: AppDatabase): StaffManagRepository {
        return StaffManagRepository(providedb)
    }

    @Provides
    fun provideLoginRepository(providedb: AppDatabase): LoginRepository {
        return LoginRepository(providedb)
    }

    @Provides
    fun provideGstTaxRepository(providedb: AppDatabase): GstTaxRepository {
        return GstTaxRepository(providedb)
    }


    @Provides
    fun provideSavedOrderRepository(providedb: AppDatabase): SavedOrderRepository {
        return SavedOrderRepository(providedb)
    }

    @Provides
    fun providePreferencesHelper(@ApplicationContext appContext: Context): PreferencesHelper {
        return PreferencesHelper(appContext)
    }

    @Provides
    fun provideCreditRepository(providedb: AppDatabase): CreditRepository {
        return CreditRepository(providedb)
    }

}