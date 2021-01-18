package com.codinginflow.mvvmtodo.ui.addedittasks

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.codinginflow.mvvmtodo.utils.Constants
import com.codinginflow.mvvmtodo.utils.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    val viewModel: AddEditTaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //super.onViewCreated(view, savedInstanceState)
        val binding: FragmentAddEditTaskBinding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.setText("Created ${viewModel.task?.createdDateFormatted}")
            checkBoxImportant.isChecked = viewModel.importance
            checkBoxImportant.jumpDrawablesToCurrentState()
            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.importance = isChecked
            }
            fabSaveTask.setOnClickListener() {
                viewModel.onSaveBtnClick()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditFlow.collect { event ->
                when(event){
                    is AddEditTaskEvents.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            Constants.REQUETS_KEY_ADDEDIT_TO_TASKS,
                            bundleOf(Constants.ADDEDIT_KEY_PAIR to  event.flag)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditTaskEvents.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(),event.message,Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive

            }
        }
    }

}