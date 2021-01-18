package com.codinginflow.mvvmtodo.db

import android.provider.SyncStateContract
import androidx.room.*
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.utils.Constants
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    fun getTasks(searchQuery: String, sortQuery: String,hideCompleted: Boolean): Flow<List<Task>> =
        when (sortQuery) {
            Constants.SORT_BY_NAME -> getTasksByName(searchQuery,hideCompleted)
            Constants.SORT_BY_DATE -> getTasksByDate(searchQuery,hideCompleted)
            else -> getTasksByDate(searchQuery,hideCompleted)
        }


    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted OR completed = 0)AND name LIKE '%'|| :searchQuery || '%' ORDER BY important DESC,name")
    fun getTasksByName(searchQuery: String,hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted OR completed = 0)AND name LIKE '%'|| :searchQuery || '%' ORDER BY important DESC , created")
    fun getTasksByDate(searchQuery: String,hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteAllTasks()
}