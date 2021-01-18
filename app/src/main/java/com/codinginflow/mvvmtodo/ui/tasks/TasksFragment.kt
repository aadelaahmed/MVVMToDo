package com.codinginflow.mvvmtodo.ui.tasks

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentTasksBinding
import com.codinginflow.mvvmtodo.utils.Constants
import com.codinginflow.mvvmtodo.utils.OnTextSearchChanged
import com.codinginflow.mvvmtodo.utils.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


private const val TAG = "TasksFragment"

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnNoteClickListener {
    private val viewModel: TasksViewModel by viewModels()
    private lateinit var searchView: SearchView
    val tasksAdapter = TasksAdapter(this)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding: FragmentTasksBinding = FragmentTasksBinding.bind(view)
        binding.apply {
            recyclerViewTasks.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = tasksAdapter
            }
            fabAddTask.setOnClickListener() {
                viewModel.onFabClicked()
            }
        }

        viewModel.liveSearch.observe(viewLifecycleOwner)
        { liveList ->
            tasksAdapter.submitList(liveList)
        }

        val itemTouchHelper = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = tasksAdapter.currentList.get(position)
                    viewModel.onSwipeNoteItem(task)
                }
            }
        }
        ItemTouchHelper(itemTouchHelper).attachToRecyclerView(binding.recyclerViewTasks)

        setHasOptionsMenu(true)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksChannelFlow.collect { taskEvent ->
                when (taskEvent) {
                    is TasksViewModel.TasksEvent.ShowUpdatedNoteMessage ->
                        Snackbar.make(view, taskEvent.message, Snackbar.LENGTH_SHORT).show()
                    is TasksViewModel.TasksEvent.ShowUnDoDeleteNote ->
                        Snackbar.make(view, taskEvent.message, Snackbar.LENGTH_SHORT)
                            .setAction("Undo") {
                                viewModel.onUnDoDeltedNote(taskEvent.task)
                            }.show()
                    is TasksViewModel.TasksEvent.NavigationToAddScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                null,
                                taskEvent.title
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.NavigationToEditScreen -> {
                        val action =
                            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                                taskEvent.task,
                                taskEvent.title
                            )
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.FromAddEventScreen ->
                        Snackbar.make(requireView(), taskEvent.message, Snackbar.LENGTH_SHORT)
                            .show()
                    is TasksViewModel.TasksEvent.FromEditEventScreen ->
                        Snackbar.make(requireView(), taskEvent.message, Snackbar.LENGTH_SHORT)
                            .show()
                    TasksViewModel.TasksEvent.NavigateConfirmDeletedTasks -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteTasksFragment()
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.EnableDarkMode -> {
                        //TODO --> enable switching into dark mode
//                        if (taskEvent.currentThemeState == true)
//                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                        else
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }.exhaustive
            }
        }
        setFragmentResultListener(Constants.REQUETS_KEY_ADDEDIT_TO_TASKS) { _, bundle ->
            val flag = bundle.getInt(Constants.ADDEDIT_KEY_PAIR)
            viewModel.onAddEditResult(flag)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentTheme = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK as Int
//        if (currentTheme == Configuration.UI_MODE_NIGHT_YES)
//                viewModel.onDarkModeChanged(Configuration.UI_MODE_NIGHT_YES)
//        else
//                viewModel.onDarkModeChanged(Configuration.UI_MODE_NIGHT_NO)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

//    fun setupTheme(context: Context): Context? {
//        var context = context
//        val res: Resources = context.resources
//        var mode: Int = res.getConfiguration().uiMode
//        when (activity.theme(context)) {
//            MetalIconFactory.DARK -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                mode = Configuration.UI_MODE_NIGHT_YES
//            }
//            MetalIconFactory.LIGHT -> {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                mode = Configuration.UI_MODE_NIGHT_NO
//            }
//            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO)
//        }
//        val config = Configuration(res.getConfiguration())
//        config.uiMode = mode
//        if (Build.VERSION.SDK_INT >= 17) {
//            context = context.createConfigurationContext(config)
//        } else {
//            res.updateConfiguration(config, res.getDisplayMetrics())
//        }
//        return context
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tasks_menu, menu)
        val searchItem = menu.findItem(R.id.search_item)
        searchView = searchItem.actionView as SearchView
        val pendingQuery = viewModel.searchQuery.value as String
        if (pendingQuery.isNotEmpty() && pendingQuery != null)
        {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }
        searchView.OnTextSearchChanged { newSearchString ->
            viewModel.searchQuery.value = newSearchString
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.hide_completed_tasks).isChecked =
                viewModel.storeFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)
        return when (item.itemId) {
            R.id.sort_by_name -> {
                viewModel.updateSortOrder(Constants.SORT_BY_NAME)
                //Log.d(TAG, "onOptionsItemSelected: sort by name clicked")
                true
            }
            R.id.sort_by_date -> {
                viewModel.updateSortOrder(Constants.SORT_BY_DATE)
                ///Log.d(TAG, "onOptionsItemSelected: sort by date clicked")
                true
            }
            R.id.hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.updateHideCompleted(item.isChecked)
                true
            }
            R.id.delete_all_tasks -> {
                viewModel.onDeleteAllCompletedTasks()
                true
            }
            R.id.dark_mode_item -> {
                item.isChecked = !item.isChecked
                viewModel.onDarkModeUpdated(item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNoteItemClick(task: Task) {
        viewModel.onNoteClicked(task)
    }

    override fun onNoteCompletedClick(task: Task, isCompleted: Boolean) {
        viewModel.onClickNoteCompleted(task, isCompleted)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}