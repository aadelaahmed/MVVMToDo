package com.codinginflow.mvvmtodo.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.ADD_TASK_RESULT_OK
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.db.TasksDao
import com.codinginflow.mvvmtodo.utils.PreferenceManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    val tasksDao: TasksDao,
    val preferenceManager: PreferenceManager,
    @Assisted state: SavedStateHandle
) : ViewModel() {
    val tasksEventChannel = Channel<TasksEvent>()
    val tasksChannelFlow = tasksEventChannel.receiveAsFlow()
    val searchQuery = state.getLiveData("searchQuery", "")
    //this live data updated when we use text change listener (extension function) of search view
//   val sortQuery = MutableStateFlow("")
//   val hideCompleted = MutableStateFlow(false)

    val storeFlow = preferenceManager.preferenceFlow
    val listFlow = combine(
        searchQuery.asFlow(), storeFlow
    ) { searchQuery, storeFlow ->
        Pair(searchQuery, storeFlow)
    }.flatMapLatest { pair ->
        val filterPreference = pair.second
        onDarkModeChanged(filterPreference.currentThemeState)
        tasksDao.getTasks(pair.first, filterPreference.sortQuery, filterPreference.hideCompleted)
    }

//   private val searchFlow = combine(searchQuery,sortQuery,hideCompleted){
//       searchQuery , sortQuery,hideCompleted ->
//       Triple(searchQuery,sortQuery,hideCompleted)
//   }
//       .flatMapLatest {triple ->
//            tasksDao.getTasks(triple.first,triple.second,triple.third)
//    }

    val liveSearch = listFlow.asLiveData()

    fun updateHideCompleted(hideCompleted: Boolean) {
        viewModelScope.launch {
            preferenceManager.updateHideCompleted(hideCompleted)
        }
    }

    fun updateSortOrder(sortOrder: String) {
        viewModelScope.launch {
            preferenceManager.updateSortOrder(sortOrder)
        }
    }

    fun onClickNoteCompleted(task: Task, isCompleted: Boolean) {
        viewModelScope.launch {
            tasksDao.update(task.copy(completed = isCompleted))
            if (isCompleted)
                tasksEventChannel.send(TasksEvent.ShowUpdatedNoteMessage("Task Completed"))
            else
                tasksEventChannel.send(TasksEvent.ShowUpdatedNoteMessage("Task UnCompleted"))
        }
    }

    fun onSwipeNoteItem(task: Task) {
        viewModelScope.launch {
            tasksDao.delete(task)
            tasksEventChannel.send(TasksEvent.ShowUnDoDeleteNote(task, "Task Deleted"))
        }
    }

    fun onUnDoDeltedNote(task: Task) {
        viewModelScope.launch {
            tasksDao.insert(task)
        }
    }

    fun onFabClicked() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigationToAddScreen("Add Task"))
        }
    }

    fun onNoteClicked(task: Task) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigationToEditScreen(task, "Edit Task"))
        }
    }

    fun onAddEditResult(flag: Int) {
        if (flag == ADD_TASK_RESULT_OK)
            sendAddMessage()
        else
            sendEditMessage()
    }

    fun sendAddMessage() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.FromAddEventScreen("Task Added"))
        }
    }

    fun sendEditMessage() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.FromEditEventScreen("Task Updated"))
        }

    }

    fun onDeleteAllCompletedTasks() {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.NavigateConfirmDeletedTasks)
        }
    }

    fun onDarkModeUpdated(checked: Boolean) {
       viewModelScope.launch {
           preferenceManager.updateDarkMode(checked)
       }
    }

    fun onDarkModeChanged(updatedThemeState: Boolean) {
        viewModelScope.launch {
            tasksEventChannel.send(TasksEvent.EnableDarkMode(updatedThemeState))
        }
    }

    sealed class TasksEvent {
        data class ShowUpdatedNoteMessage(val message: String) : TasksEvent()
        data class ShowUnDoDeleteNote(val task: Task, val message: String) : TasksEvent()
        data class NavigationToAddScreen(val title: String) : TasksEvent()
        data class NavigationToEditScreen(val task: Task, val title: String) : TasksEvent()
        data class FromAddEventScreen(val message: String) : TasksEvent()
        data class FromEditEventScreen(val message: String) : TasksEvent()
        object NavigateConfirmDeletedTasks : TasksEvent()
        data class EnableDarkMode(val currentThemeState: Boolean) : TasksEvent()
    }
}

