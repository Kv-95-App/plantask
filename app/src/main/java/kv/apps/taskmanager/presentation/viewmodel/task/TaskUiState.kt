package kv.apps.taskmanager.presentation.viewmodel.task

import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.domain.model.User
import java.time.LocalDate

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val selectedTasks: List<Task> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val selectedTask: Task? = null,
    val projectTeamMembers: List<User> = emptyList(),
    val taskAssignedUsersInitials: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
    val currentTaskComments: List<TaskComment> = emptyList(),
    val isCommentLoading: Boolean = false
)