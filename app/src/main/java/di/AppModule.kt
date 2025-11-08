package com.alarmstopper.di

import android.content.Context
import androidx.room.Room
import com.alarmstopper.data.local.AlarmDatabase
import com.alarmstopper.data.repository.*
import com.alarmstopper.domain.manager.AlarmScheduler
import com.alarmstopper.domain.usecase.*
import com.alarmstopper.presentation.alarmlist.AlarmListViewModel
import com.alarmstopper.presentation.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.UUID

val appModule = module {

    // Firebase
    single { FirebaseAuth.getInstance() }
    single {
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
        }
    }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AlarmDatabase::class.java,
            "alarm_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<AlarmDatabase>().alarmDao() }

    // Device ID
    single {
        val prefs = androidContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val deviceId = prefs.getString("device_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
        deviceId
    }

    // Repositories
    single<AuthRepository> {
        FirebaseAuthRepository(get())
    }

    single {
        FirebaseSyncRepository(
            database = get(),
            auth = get(),
            deviceId = get()
        )
    }

    single<AlarmRepository> {
        AlarmRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get()
        )
    }

    // Domain Layer
    single { AlarmScheduler(androidContext()) }

    single { InitializeAuthUseCase(get()) }
    single { UpgradeToGoogleUseCase(get()) }
    single { CreateAlarmUseCase(get(), get()) }
    single {
        StopAllAlarmsUseCase(
            alarmRepository = get(),
            syncRepository = get(),
            alarmScheduler = get()
        )
    }
    single {
        StopAlarmsInRangeUseCase(
            alarmRepository = get(),
            syncRepository = get(),
            alarmScheduler = get()
        )
    }

    // ViewModels
    viewModel { AuthViewModel(get(), get()) }
    viewModel {
        AlarmListViewModel(
            alarmRepository = get(),
            createAlarmUseCase = get(),
            stopAllAlarmsUseCase = get(),
            stopAlarmsInRangeUseCase = get(),
            auth = get(),
            deviceId = get()
        )
    }
}
