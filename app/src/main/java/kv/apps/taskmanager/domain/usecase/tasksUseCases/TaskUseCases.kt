package kv.apps.taskmanager.domain.usecase.tasksUseCases

import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class TaskUseCases @Inject constructor(
    private val repository: TaskRepository
) {
    suspend fun getTasksForProject(projectId: String) = repository.getTasksForProject(projectId)

    suspend fun addTaskToProject(projectId: String, task: Task) = repository.addTaskToProject(projectId, task)

    suspend fun updateTaskInProject(projectId: String, task: Task) = repository.updateTaskInProject(projectId, task)

    suspend fun deleteTaskFromProject(projectId: String, taskId: String) = repository.deleteTaskFromProject(projectId, taskId)

    suspend fun filterTasksByDueDate(projectId: String, dueDate: LocalDate) = repository.filterTasksByDueDate(projectId, dueDate)

    suspend fun getTaskByIdFromProject(projectId: String, taskId: String) = repository.getTaskByIdFromProject(projectId, taskId)

    suspend fun getProjectUsers(projectId: String) = repository.getProjectUsers(projectId)
}