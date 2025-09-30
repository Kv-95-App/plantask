package kv.apps.taskmanager.data.repositoryImpl

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.data.connectivity.ConnectivityObserver
import kv.apps.taskmanager.data.local.dao.TaskDao
import kv.apps.taskmanager.data.local.entity.SyncAction
import kv.apps.taskmanager.data.local.mapper.toDomain
import kv.apps.taskmanager.data.local.mapper.toDomainList
import kv.apps.taskmanager.data.local.mapper.toEntity
import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import kv.apps.taskmanager.data.sync.SyncManager
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.TaskRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class OfflineFirstTaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val taskRemoteDataSource: TaskRemoteDataSource,
    private val connectivityObserver: ConnectivityObserver,
    private val syncManager: SyncManager
) : TaskRepository {

    override suspend fun getTasksForProject(projectId: String): List<Task> {
        if (connectivityObserver.isOnline()) {
            try {
                syncManager.forceSyncProject(projectId)
            } catch (_: Exception) {
            }
        }
        return taskDao.getTasksForProjectSync(projectId).toDomainList()
    }

    override suspend fun addTaskToProject(projectId: String, task: Task) {
        val taskWithId = if (task.id.isEmpty()) {
            task.copy(id = UUID.randomUUID().toString())
        } else task

        val taskEntity = taskWithId.toEntity(
            needsSync = true,
            syncAction = SyncAction.CREATE
        )
        taskDao.insertTask(taskEntity)

        if (connectivityObserver.isOnline()) {
            try {
                taskRemoteDataSource.addTaskToProject(projectId, taskWithId)
                taskDao.markTaskAsSynced(taskWithId.id)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun updateTaskInProject(projectId: String, task: Task) {
        val taskEntity = task.toEntity(
            needsSync = true,
            syncAction = SyncAction.UPDATE
        )
        taskDao.insertTask(taskEntity)

        if (connectivityObserver.isOnline()) {
            try {
                taskRemoteDataSource.updateTaskInProject(projectId, task)
                taskDao.markTaskAsSynced(task.id)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun deleteTaskFromProject(projectId: String, taskId: String) {
        taskDao.markTaskAsDeleted(taskId)

        if (connectivityObserver.isOnline()) {
            try {
                taskRemoteDataSource.deleteTaskFromProject(projectId, taskId)
                taskDao.deleteTask(taskId)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task? {
        if (connectivityObserver.isOnline()) {
            try {
                val remoteTask = taskRemoteDataSource.getTaskByIdFromProject(projectId, taskId)
                if (remoteTask != null) {
                    taskDao.insertTask(remoteTask.toEntity())
                }
            } catch (_: Exception) {
            }
        }

        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun getTasksSortedByDueDate(
        projectId: String,
        ascending: Boolean
    ): List<Task> {
        return if (ascending) {
            taskDao.getTasksSortedByDueDateAsc(projectId).toDomainList()
        } else {
            taskDao.getTasksSortedByDueDateDesc(projectId).toDomainList()
        }
    }

    override suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task> {
        return taskDao.getTasksByDueDate(projectId, date.toString()).toDomainList()
    }

    override suspend fun getProjectUsers(projectId: String): Flow<List<User>> {
        return taskRemoteDataSource.getProjectUsers(projectId)
    }

    override suspend fun getMembersOfTasks(projectId: String, taskId: String): List<User> {
        return if (connectivityObserver.isOnline()) {
            try {
                taskRemoteDataSource.getMembersOfTasks(projectId, taskId)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun getTaskAssignedUsersInitials(
        projectId: String,
        taskId: String
    ): Map<String, String> {
        return if (connectivityObserver.isOnline()) {
            try {
                taskRemoteDataSource.getTaskAssignedUsersInitials(projectId, taskId)
            } catch (_: Exception) {
                emptyMap()
            }
        } else {
            emptyMap()
        }
    }

    override suspend fun addCommentToTask(
        projectId: String,
        taskId: String,
        message: String
    ): Result<String> {
        return if (connectivityObserver.isOnline()) {
            taskRemoteDataSource.addCommentToTask(projectId, taskId, message)
        } else {
            Result.failure(Exception("Comments require internet connection"))
        }
    }

    override suspend fun getTaskComments(
        projectId: String,
        taskId: String,
        limit: Int
    ): List<TaskComment> {
        return if (connectivityObserver.isOnline()) {
            try {
                taskRemoteDataSource.getTaskComments(projectId, taskId, limit)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
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
        return if (connectivityObserver.isOnline()) {
            taskRemoteDataSource.editTaskComment(projectId, taskId, commentId, newMessage)
        } else {
            Result.failure(Exception("Editing comments requires internet connection"))
        }
    }

    override suspend fun deleteTaskComment(
        projectId: String,
        taskId: String,
        commentId: String
    ): Result<Unit> {
        return if (connectivityObserver.isOnline()) {
            taskRemoteDataSource.deleteTaskComment(projectId, taskId, commentId)
        } else {
            Result.failure(Exception("Deleting comments requires internet connection"))
        }
    }
}