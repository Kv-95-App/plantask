package kv.apps.taskmanager.domain.repository

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.domain.model.User
import java.time.LocalDate

interface TaskRepository {
    suspend fun getTasksForProject(projectId: String): List<Task>
    suspend fun addTaskToProject(projectId: String, task: Task)
    suspend fun updateTaskInProject(projectId: String, task: Task)
    suspend fun deleteTaskFromProject(projectId: String, taskId: String)
    suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task?
    suspend fun getTasksSortedByDueDate(projectId: String, ascending: Boolean): List<Task>
    suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task>
    suspend fun getProjectUsers(projectId: String): Flow<List<User>>
    suspend fun getMembersOfTasks(projectId: String, taskId: String): List<User>
    suspend fun getTaskAssignedUsersInitials(projectId: String, taskId: String): Map<String, String>
    suspend fun addCommentToTask(projectId: String, taskId: String, message: String): Result<String>
    suspend fun getTaskComments(projectId: String, taskId: String, limit: Int = 50): List<TaskComment>
    fun observeTaskComments(projectId: String, taskId: String, limit: Int = 50): Flow<List<TaskComment>>
    suspend fun editTaskComment(
        projectId: String,
        taskId: String,
        commentId: String,
        newMessage: String
    ): Result<Unit>
    suspend fun deleteTaskComment(
        projectId: String,
        taskId: String,
        commentId: String
    ): Result<Unit>
}