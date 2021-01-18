package com.codinginflow.mvvmtodo.db

import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.di.RoomModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TasksDatabase() : RoomDatabase() {
    abstract fun getDao(): TasksDao

    class TasksCallback @Inject constructor(private val provider: Provider<TasksDatabase>,@RoomModule.ApplicationScope private val coroutineScope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val tasksDao = provider.get().getDao()
            coroutineScope.launch {
                tasksDao.insert(Task(name = "Azkar", completed = true))
                tasksDao.insert(Task(name = "Doha", important = true))
                tasksDao.insert(Task("BreakFast"))
                tasksDao.insert(Task("Session 45 minutes"))
                tasksDao.insert(Task("Break with whatsapp"))
                tasksDao.insert(Task("Session 45 minutes", important = true))
            }

        }
    }
}