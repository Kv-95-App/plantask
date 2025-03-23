package kv.apps.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.usecase.tasksUseCases.TaskUseCases
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    // State for tasks in a project
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    // State for selected tasks (filtered by due date)
    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks: StateFlow<List<Task>> = _selectedTasks

    // State for selected date (used for filtering tasks)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    // State for loading
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    // State for error type
    private val _errorType = MutableStateFlow<TaskErrorType?>(null)
    val errorType: StateFlow<TaskErrorType?> = _errorType

    // State for error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // State for the selected task
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    // Load tasks for a specific project
    fun loadTasksForProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _tasks.value = taskUseCases.getTasksForProject(projectId)
                filterTasksByDueDate(projectId, _selectedDate.value)
                _errorType.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorType.value = TaskErrorType.LOAD_ERROR
                _errorMessage.value = "Failed to load tasks: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Add a task to a project
    fun addTaskToProject(projectId: String, task: Task) {
        viewModelScope.launch {
            try {
                taskUseCases.addTaskToProject(projectId, task)
                refreshTasks(projectId)
                _errorType.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorType.value = TaskErrorType.ADD_ERROR
                _errorMessage.value = "Failed to add task: ${e.message}"
            }
        }
    }

    // Update a task in a project
    fun updateTaskInProject(projectId: String, task: Task) {
        viewModelScope.launch {
            try {
                taskUseCases.updateTaskInProject(projectId, task)
                _selectedTask.value = task
                refreshTasks(projectId)
                _errorType.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorType.value = TaskErrorType.UPDATE_ERROR
                _errorMessage.value = "Failed to update task: ${e.message}"
            }
        }
    }

    // Delete a task from a project
    fun deleteTaskFromProject(projectId: String, taskId: String) {
        viewModelScope.launch {
            try {
                taskUseCases.deleteTaskFromProject(projectId, taskId)
                refreshTasks(projectId)
                _errorType.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorType.value = TaskErrorType.DELETE_ERROR
                _errorMessage.value = "Failed to delete task: ${e.message}"
            }
        }
    }

    // Fetch a specific task by ID from a project
    fun fetchTaskByIdFromProject(projectId: String, taskId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _selectedTask.value = taskUseCases.getTaskByIdFromProject(projectId, taskId)
                _errorType.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _selectedTask.value = null
                _errorType.value = TaskErrorType.FETCH_ERROR
                _errorMessage.value = "Failed to fetch task: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Filter tasks by due date for a specific project
    private fun filterTasksByDueDate(projectId: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                _selectedTasks.value = taskUseCases.filterTasksByDueDate(projectId, date)
                _errorType.value = null
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorType.value = TaskErrorType.FILTER_ERROR
                _errorMessage.value = "Failed to filter tasks: ${e.message}"
            }
        }
    }

    // Update the selected date and filter tasks
    fun selectDate(projectId: String, date: LocalDate) {
        _selectedDate.value = date
        filterTasksByDueDate(projectId, date)
    }

    // Helper function to refresh the task list
    private fun refreshTasks(projectId: String) {
        loadTasksForProject(projectId)
    }
}