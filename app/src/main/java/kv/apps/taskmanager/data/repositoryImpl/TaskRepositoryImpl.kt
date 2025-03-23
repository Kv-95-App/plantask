package kv.apps.taskmanager.data.repositoryImpl

import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.repository.TaskRepository
import java.time.LocalDate
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskRemoteDataSource: TaskRemoteDataSource
) : TaskRepository {

    override suspend fun getTasksForProject(projectId: String): List<Task> {
        return taskRemoteDataSource.getTasksForProject(projectId)
    }

    override suspend fun addTaskToProject(projectId: String, task: Task) {
        taskRemoteDataSource.addTaskToProject(projectId, task)
    }

    override suspend fun updateTaskInProject(projectId: String, task: Task) {
        taskRemoteDataSource.updateTaskInProject(projectId, task)
    }

    override suspend fun deleteTaskFromProject(projectId: String, taskId: String) {
        taskRemoteDataSource.deleteTaskFromProject(projectId, taskId)
    }

    override suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task? {
        return taskRemoteDataSource.getTaskByIdFromProject(projectId, taskId)
    }

    override suspend fun getTasksSortedByDueDate(projectId: String, ascending: Boolean): List<Task> {
        return taskRemoteDataSource.getTasksSortedByDueDate(projectId, ascending)
    }

    override suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task> {
        return taskRemoteDataSource.filterTasksByDueDate(projectId, date)
    }
}