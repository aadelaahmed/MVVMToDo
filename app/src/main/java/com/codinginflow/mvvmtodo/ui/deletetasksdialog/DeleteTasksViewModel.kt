package com.codinginflow.mvvmtodo.ui.deletetasksdialog

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.db.TasksDao
import com.codinginflow.mvvmtodo.di.RoomModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DeleteTasksViewModel @ViewModelInject constructor(
    private val tasksDao: TasksDao,
    @RoomModule.ApplicationScope val  applicationScope: CoroutineScope //defined in di package
): ViewModel() {
    private val deleteTasksChannel = Channel<DeleteTasksEvent>()
    val deletedTasksFlow = deleteTasksChannel.receiveAsFlow()
    fun onConfirmDeleteCompletedTasks() { applicationScope.launch {
        tasksDao.deleteAllTasks()
        deleteTasksChannel.send(DeleteTasksEvent.ConfirmDeleteCompletedTasks("Completed Tasks Deleted"))
    }
    }
}
sealed class DeleteTasksEvent{
    data class ConfirmDeleteCompletedTasks(val message: String) : DeleteTasksEvent()
}