package kv.apps.taskmanager.domain.repository

import kv.apps.taskmanager.domain.model.Task
import java.time.LocalDate

interface TaskRepository {
    suspend fun getTasksForProject(projectId: String): List<Task>
    suspend fun addTaskToProject(projectId: String, task: Task)
    suspend fun updateTaskInProject(projectId: String, task: Task)
    suspend fun deleteTaskFromProject(projectId: String, taskId: String)
    suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task?
    suspend fun getTasksSortedByDueDate(projectId: String, ascending: Boolean): List<Task>
    suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task>
}