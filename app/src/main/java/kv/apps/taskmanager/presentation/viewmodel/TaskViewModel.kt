package kv.apps.taskmanager.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.usecase.tasksUseCases.TaskUseCases
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _selectedTasks = MutableStateFlow<List<Task>>(emptyList())
    val selectedTasks: StateFlow<List<Task>> = _selectedTasks

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorType = MutableStateFlow<TaskErrorType?>(null)
    val errorType: StateFlow<TaskErrorType?> = _errorType

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private val _projectTeamMembers = MutableStateFlow<List<User>>(emptyList())
    val projectTeamMembers: StateFlow<List<User>> = _projectTeamMembers

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

    fun getProjectUsers(projectId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                taskUseCases.getProjectUsers(projectId).collect { users ->
                    _projectTeamMembers.value = users
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading team members", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectDate(projectId: String, date: LocalDate) {
        _selectedDate.value = date
        filterTasksByDueDate(projectId, date)
    }

    private fun refreshTasks(projectId: String) {
        loadTasksForProject(projectId)
    }
}