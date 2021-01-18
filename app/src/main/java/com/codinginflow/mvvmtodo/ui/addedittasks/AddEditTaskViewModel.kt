package com.codinginflow.mvvmtodo.ui.addedittasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codinginflow.mvvmtodo.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.EDIT_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.MainActivity
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.db.TasksDao
import com.codinginflow.mvvmtodo.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class AddEditTaskViewModel @ViewModelInject constructor(
        private val tasksDao: TasksDao,
        @Assisted val state: SavedStateHandle
) : ViewModel() {
    private val addEditChannel = Channel<AddEditTaskEvents>()
    val addEditFlow = addEditChannel.receiveAsFlow()
    //when SavedStateHandle connected (using hilt) with the recieved fragment (task argument),state fetch its argument automatically
    val task = state.get<Task>(Constants.TASK_INSTANCE_SAVE_STATE)
    var taskName = state.get<String>(Constants.TASK_NAME_SAVE_STATE) ?: task?.name ?: ""
        set(value) {
            field = value
            state.set(Constants.TASK_NAME_SAVE_STATE,value)
        }
    var importance = state.get<Boolean>(Constants.IMPORTANCE_SAVE_STATE) ?: task?.important ?: false
        set(value) {
            field = value
            state.set(Constants.IMPORTANCE_SAVE_STATE,value)
        }


    fun onSaveBtnClick() {
        if (taskName.isBlank())
        {
            onShowInvalidInputMessage()
            return
        }
        if (task != null)
            onEditTask(task.copy(name = taskName, important  = importance))
        else
           onAddTask(Task(name = taskName,important = importance))
    }

    private fun onAddTask(task: Task) {
        viewModelScope.launch {
            tasksDao.insert(task)
            addEditChannel.send(AddEditTaskEvents.NavigateBackWithResult(ADD_TASK_RESULT_OK))
        }
    }

    private fun onEditTask(task: Task) {
        viewModelScope.launch {
            tasksDao.update(task)
            addEditChannel.send(AddEditTaskEvents.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
        }
    }

    private fun onShowInvalidInputMessage() {
        viewModelScope.launch {
            addEditChannel.send(AddEditTaskEvents.ShowInvalidInputMessage("Invalid Task Name"))
        }
    }
}
sealed class AddEditTaskEvents{
    data class ShowInvalidInputMessage (val message:String) : AddEditTaskEvents()
    data class NavigateBackWithResult (val flag : Int) : AddEditTaskEvents()
}