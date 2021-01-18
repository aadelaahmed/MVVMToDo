package com.codinginflow.mvvmtodo.ui.deletetasksdialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.codinginflow.mvvmtodo.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
@AndroidEntryPoint
class DeleteTasksFragment : DialogFragment() {
    private val viewModel : DeleteTasksViewModel by viewModels()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(requireContext())
            .setTitle("Delete Completed Tasks")
            .setMessage("Are you want to delete all completed tasks ?")
            .setNegativeButton("No"){ _,_ ->
            }
            .setPositiveButton("Yes"){_,_->
                viewModel.onConfirmDeleteCompletedTasks()
//                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//                    viewModel.deletedTasksFlow.collect {deleteEvent ->
//                        when(deleteEvent)
//                        {
//                            is DeleteTasksEvent.ConfirmDeleteCompletedTasks ->
//
//                        }.exhaustive
//                    }
//                }
            }
            .create()
    }
}