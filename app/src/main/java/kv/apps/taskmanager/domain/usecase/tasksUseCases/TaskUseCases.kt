package kv.apps.taskmanager.domain.usecase.tasksUseCases

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class TaskUseCases @Inject constructor(
    private val repository: TaskRepository
) {
    suspend fun getTasksForProject(projectId: String): List<Task> {
        return repository.getTasksForProject(projectId)
    }

    suspend fun addTaskToProject(projectId: String, task: Task) {
        repository.addTaskToProject(projectId, task)
    }

    suspend fun updateTaskInProject(projectId: String, task: Task) {
        repository.updateTaskInProject(projectId, task)
    }

    suspend fun deleteTaskFromProject(projectId: String, taskId: String) {
        repository.deleteTaskFromProject(projectId, taskId)
    }

    suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task? {
        return repository.getTaskByIdFromProject(projectId, taskId)
    }

    // Task Filtering/Sorting
    suspend fun filterTasksByDueDate(projectId: String, dueDate: LocalDate): List<Task> {
        return repository.filterTasksByDueDate(projectId, dueDate)
    }


    suspend fun getMembersOfTasks(projectId: String,taskId: String): List<User> {
        return repository.getMembersOfTasks(projectId,taskId)
    }

    suspend fun getTaskAssignedUsersInitials(projectId: String, taskId: String): Map<String, String> {
        return repository.getTaskAssignedUsersInitials(projectId, taskId)
    }

    suspend fun addCommentToTask(
        projectId: String,
        taskId: String,
        message: String
    ): Result<String> {
        if (message.isBlank()) {
            return Result.failure(IllegalArgumentException("Comment message cannot be empty"))
        }
        return repository.addCommentToTask(projectId, taskId, message)
    }

    suspend fun getTaskComments(
        projectId: String,
        taskId: String,
        limit: Int = 50
    ): List<TaskComment> {
        return repository.getTaskComments(projectId, taskId, limit)
    }

    fun observeTaskComments(
        projectId: String,
        taskId: String,
        limit: Int = 50
    ): Flow<List<TaskComment>> {
        return repository.observeTaskComments(projectId, taskId, limit)
    }

    suspend fun editTaskComment(
        projectId: String,
        taskId: String,
        commentId: String,
        newMessage: String
    ): Result<Unit> {
        if (newMessage.isBlank()) {
            return Result.failure(IllegalArgumentException("Comment message cannot be empty"))
        }
        return repository.editTaskComment(projectId, taskId, commentId, newMessage)
    }

    suspend fun deleteTaskComment(
        projectId: String,
        taskId: String,
        commentId: String
    ): Result<Unit> {
        return repository.deleteTaskComment(projectId, taskId, commentId)
    }
}