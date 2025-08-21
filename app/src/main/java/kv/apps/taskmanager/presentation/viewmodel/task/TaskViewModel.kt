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
import kv.apps.taskmanager.domain.model.TaskComment
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

    private val _taskMembersFlow = MutableStateFlow<Map<String, List<User>>>(emptyMap())
    val taskMembersFlow: StateFlow<Map<String, List<User>>> = _taskMembersFlow.asStateFlow()

    private val _commentsState = MutableStateFlow(emptyMap<String, List<TaskComment>>())
    val commentsState: StateFlow<Map<String, List<TaskComment>>> = _commentsState.asStateFlow()

    private val _initialsCache = mutableMapOf<String, String>()

    fun clearTaskState() {
        _uiState.update {
            it.copy(
                selectedTask = null,
                isLoading = true,
                errorMessage = null
            )
        }
    }

    fun fetchAssignedUsersInitials(projectId: String, taskId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentInitials = currentState.taskAssignedUsersInitials

            if (currentInitials.isEmpty()) {
                try {
                    val initials = taskUseCases.getTaskAssignedUsersInitials(projectId, taskId)
                    _initialsCache.putAll(initials)
                    _uiState.update {
                        it.copy(
                            taskAssignedUsersInitials = initials
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Failed to fetch initials: ${e.message}"
                        )
                    }
                }
            }
        }
    }

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
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.LOAD_ERROR, "Failed to load tasks: ${e.message}")
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
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.ADD_ERROR, "Failed to add task: ${e.message}")
            }
        }
    }

    fun fetchTaskMembers(projectId: String, taskId: String) {
        viewModelScope.launch {
            try {
                val members = taskUseCases.getMembersOfTasks(projectId, taskId)
                _taskMembersFlow.update { current ->
                    current.toMutableMap().apply {
                        put(taskId, members)
                    }
                }
                emitEvent(TaskEvent.TaskMembersLoaded)
            } catch (e: Exception) {
                emitError(TaskErrorType.MEMBERS_ERROR, "Failed to load task members: ${e.message}")
            }
        }
    }

    fun updateTaskInProject(projectId: String, task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                taskUseCases.updateTaskInProject(projectId, task)
                _uiState.update { it.copy(selectedTask = task, isLoading = false) }
                refreshTasks(projectId)
                emitEvent(TaskEvent.TaskUpdated)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.UPDATE_ERROR, "Failed to update task: ${e.message}")
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
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.DELETE_ERROR, "Failed to delete task: ${e.message}")
            }
        }
    }

    fun fetchTaskByIdFromProject(projectId: String, taskId: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val task = taskUseCases.getTaskByIdFromProject(projectId, taskId)
                _uiState.update { it.copy(selectedTask = task, isLoading = false) }
                emitEvent(TaskEvent.TaskFetched)
            } catch (e: Exception) {
                _uiState.update { it.copy(selectedTask = null, isLoading = false) }
                emitError(TaskErrorType.FETCH_ERROR, "Failed to fetch task: ${e.message}")
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

    fun addComment(projectId: String, taskId: String, message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = taskUseCases.addCommentToTask(projectId, taskId, message)
                result.onSuccess { commentId ->
                    loadTaskComments(projectId, taskId)
                    emitEvent(TaskEvent.CommentAdded(commentId))
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(TaskErrorType.COMMENT_ERROR, "Failed to add comment: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.COMMENT_ERROR, "Failed to add comment: ${e.message}")
            }
        }
    }

    fun loadTaskComments(projectId: String, taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val comments = taskUseCases.getTaskComments(projectId, taskId)
                _commentsState.update { current ->
                    current.toMutableMap().apply {
                        put(taskId, comments.sortedByDescending { it.timestamp })
                    }
                }
                _uiState.update { it.copy(isLoading = false) }
                emitEvent(TaskEvent.CommentsLoaded)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.COMMENT_ERROR, "Failed to load comments: ${e.message}")
            }
        }
    }

    fun observeTaskComments(projectId: String, taskId: String) {
        viewModelScope.launch {
            taskUseCases.observeTaskComments(projectId, taskId)
                .collect { comments ->
                    _commentsState.update { current ->
                        current.toMutableMap().apply {
                            put(taskId, comments.sortedByDescending { it.timestamp })
                        }
                    }
                }
        }
    }

    fun editComment(projectId: String, taskId: String, commentId: String, newMessage: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = taskUseCases.editTaskComment(projectId, taskId, commentId, newMessage)
                result.onSuccess {
                    loadTaskComments(projectId, taskId)
                    emitEvent(TaskEvent.CommentUpdated(commentId))
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(TaskErrorType.COMMENT_ERROR, "Failed to edit comment: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.COMMENT_ERROR, "Failed to edit comment: ${e.message}")
            }
        }
    }

    fun deleteComment(projectId: String, taskId: String, commentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = taskUseCases.deleteTaskComment(projectId, taskId, commentId)
                result.onSuccess {
                    loadTaskComments(projectId, taskId)
                    emitEvent(TaskEvent.CommentDeleted(commentId))
                }.onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    emitError(TaskErrorType.COMMENT_ERROR, "Failed to delete comment: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(TaskErrorType.COMMENT_ERROR, "Failed to delete comment: ${e.message})")
            }
        }
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