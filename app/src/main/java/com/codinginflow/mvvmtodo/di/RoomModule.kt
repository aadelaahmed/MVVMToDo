package com.codinginflow.mvvmtodo.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.codinginflow.mvvmtodo.db.TasksDatabase
import com.codinginflow.mvvmtodo.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object RoomModule {

    @Provides
    @Singleton
    fun provideTasksDatabase(app: Application,callback: TasksDatabase.TasksCallback)
     = Room.databaseBuilder(
        app,
        TasksDatabase::class.java,
        Constants.DATABASE_NAME
     )
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    @Singleton
    fun provideDao(db : TasksDatabase) = db.getDao()

    @Provides
    @Singleton
    @ApplicationScope
    fun provideCoroutineScope() = CoroutineScope(SupervisorJob())

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ApplicationScope
}