package kv.apps.taskmanager.domain.repository

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.domain.model.Task
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
}