package kv.apps.taskmanager.data.repositoryImpl

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.domain.model.User
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

    override suspend fun getProjectUsers( projectId: String): Flow<List<User>> {
        return taskRemoteDataSource.getProjectUsers(projectId)
    }
    override suspend fun getMembersOfTasks(projectId: String,taskId: String): List<User> {
        return taskRemoteDataSource.getMembersOfTasks(projectId, taskId)
    }
    override suspend fun getTaskAssignedUsersInitials(projectId: String, taskId: String): Map<String, String> {
        return taskRemoteDataSource.getTaskAssignedUsersInitials(projectId, taskId)
    }
    override suspend fun addCommentToTask(
        projectId: String,
        taskId: String,
        message: String
    ): Result<String> {
        return taskRemoteDataSource.addCommentToTask(projectId, taskId, message)
    }

    override suspend fun getTaskComments(
        projectId: String,
        taskId: String,
        limit: Int
    ): List<TaskComment> {
        return taskRemoteDataSource.getTaskComments(projectId, taskId, limit)
    }

    override fun observeTaskComments(
        projectId: String,
        taskId: String,
        limit: Int
    ): Flow<List<TaskComment>> {
        return taskRemoteDataSource.observeTaskComments(projectId, taskId, limit)
    }

    override suspend fun editTaskComment(
        projectId: String,
        taskId: String,
        commentId: String,
        newMessage: String
    ): Result<Unit> {
        return taskRemoteDataSource.editTaskComment(projectId, taskId, commentId, newMessage)
    }

    override suspend fun deleteTaskComment(
        projectId: String,
        taskId: String,
        commentId: String
    ): Result<Unit> {
        return taskRemoteDataSource.deleteTaskComment(projectId, taskId, commentId)
    }
}