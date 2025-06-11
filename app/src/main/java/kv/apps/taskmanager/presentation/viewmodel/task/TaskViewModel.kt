package kv.apps.taskmanager.presentation.viewmodel.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskEvent>()
    val events: SharedFlow<TaskEvent> = _events.asSharedFlow()

    fun loadTasksForProject(projectId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val tasks = taskUseCases.getTasksForProject(projectId)
                _uiState.update {
                    it.copy(
                        tasks = tasks,
                        isLoading = false
                    )
                }
                filterTasksByDueDate(projectId, uiState.value.selectedDate)
                emitEvent(TaskEvent.TasksLoaded)
            } catch (e: Exception) {
                emitError(TaskErrorType.LOAD_ERROR, "Failed to load tasks: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addTaskToProject(projectId: String, task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                taskUseCases.addTaskToProject(projectId, task)
                refreshTasks(projectId)
                emitEvent(TaskEvent.TaskAdded)
            } catch (e: Exception) {
                emitError(TaskErrorType.ADD_ERROR, "Failed to add task: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTaskInProject(projectId: String, task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                taskUseCases.updateTaskInProject(projectId, task)
                _uiState.update { it.copy(selectedTask = task) }
                refreshTasks(projectId)
                emitEvent(TaskEvent.TaskUpdated)
            } catch (e: Exception) {
                emitError(TaskErrorType.UPDATE_ERROR, "Failed to update task: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteTaskFromProject(projectId: String, taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                taskUseCases.deleteTaskFromProject(projectId, taskId)
                refreshTasks(projectId)
                emitEvent(TaskEvent.TaskDeleted)
            } catch (e: Exception) {
                emitError(TaskErrorType.DELETE_ERROR, "Failed to delete task: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fetchTaskByIdFromProject(projectId: String, taskId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val task = taskUseCases.getTaskByIdFromProject(projectId, taskId)
                _uiState.update { it.copy(selectedTask = task) }
                emitEvent(TaskEvent.TaskFetched)
            } catch (e: Exception) {
                _uiState.update { it.copy(selectedTask = null) }
                emitError(TaskErrorType.FETCH_ERROR, "Failed to fetch task: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun filterTasksByDueDate(projectId: String, date: LocalDate) {
        viewModelScope.launch {
            try {
                val filteredTasks = taskUseCases.filterTasksByDueDate(projectId, date)
                _uiState.update {
                    it.copy(
                        selectedTasks = filteredTasks,
                        selectedDate = date
                    )
                }
            } catch (e: Exception) {
                emitError(TaskErrorType.FILTER_ERROR, "Failed to filter tasks: ${e.message}")
            }
        }
    }

    fun getProjectUsers(projectId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                taskUseCases.getProjectUsers(projectId).collect { users ->
                    _uiState.update { it.copy(projectTeamMembers = users) }
                }
            } catch (e: Exception) {
                emitError(TaskErrorType.TEAM_MEMBERS_ERROR, "Error loading team members: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectDate(projectId: String, date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        filterTasksByDueDate(projectId, date)
    }

    private fun refreshTasks(projectId: String) {
        loadTasksForProject(projectId)
    }

    private suspend fun emitError(type: TaskErrorType, message: String) {
        _events.emit(TaskEvent.Error(type, message))
    }

    private suspend fun emitEvent(event: TaskEvent) {
        _events.emit(event)
    }


}