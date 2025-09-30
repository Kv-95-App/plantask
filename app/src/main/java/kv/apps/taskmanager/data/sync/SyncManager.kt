package kv.apps.taskmanager.data.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kv.apps.taskmanager.data.connectivity.ConnectivityObserver
import kv.apps.taskmanager.data.connectivity.NetworkStatus
import kv.apps.taskmanager.data.local.dao.ProjectDao
import kv.apps.taskmanager.data.local.dao.TaskDao
import kv.apps.taskmanager.data.local.entity.SyncAction
import kv.apps.taskmanager.data.local.mapper.toDomain
import kv.apps.taskmanager.data.local.mapper.toEntity
import kv.apps.taskmanager.data.local.mapper.toEntityList
import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
    private val taskRemoteDataSource: TaskRemoteDataSource,
    private val projectRemoteDataSource: ProjectRemoteDataSource,
    private val connectivityObserver: ConnectivityObserver
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "SyncManager"
    }

    init {
        syncScope.launch {
            connectivityObserver.observe().collect { status ->
                if (status == NetworkStatus.AVAILABLE) {
                    Log.d(TAG, "Network available, starting sync...")
                    syncAll()
                }
            }
        }
    }

    suspend fun syncAll() {
        if (!connectivityObserver.isOnline()) {
            Log.d(TAG, "Device is offline, skipping sync")
            return
        }

        try {
            Log.d(TAG, "Starting full synchronization...")

            syncProjects()
            syncTasks()

            Log.d(TAG, "Synchronization completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }

    private suspend fun syncProjects() {
        try {
            val projectsNeedingSync = projectDao.getProjectsNeedingSync()
            for (project in projectsNeedingSync) {
                try {
                    when (SyncAction.valueOf(project.syncAction)) {
                        SyncAction.CREATE -> {
                            val projectId =
                                projectRemoteDataSource.createProject(project.toDomain())
                            val updatedProject = project.copy(
                                id = projectId,
                                needsSync = false,
                                syncAction = SyncAction.NONE.name
                            )
                            projectDao.updateProject(updatedProject)
                            Log.d(TAG, "Created project: $projectId")
                        }

                        SyncAction.UPDATE -> {
                            projectRemoteDataSource.updateProject(project.id, project.toDomain())
                            projectDao.markProjectAsSynced(project.id)
                            Log.d(TAG, "Updated project: ${project.id}")
                        }

                        SyncAction.DELETE -> {
                            projectRemoteDataSource.deleteProject(project.id)
                            projectDao.deleteProject(project.id)
                            Log.d(TAG, "Deleted project: ${project.id}")
                        }

                        SyncAction.NONE -> {
                            projectDao.markProjectAsSynced(project.id)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync project ${project.id}", e)
                }
            }

            val remoteProjects = projectRemoteDataSource.getAllProjectsForUser()
            val localProjects = projectDao.getAllProjectsSync()

            val remoteProjectIds = remoteProjects.map { it.id }.toSet()
            val localProjectIds = localProjects.map { it.id }.toSet()

            val newProjects = remoteProjects.filter { !localProjectIds.contains(it.id) }
            if (newProjects.isNotEmpty()) {
                projectDao.insertProjects(newProjects.toEntityList())
                Log.d(TAG, "Inserted ${newProjects.size} new projects from remote")
            }

            for (remoteProject in remoteProjects) {
                val localProject = localProjects.find { it.id == remoteProject.id }
                if (localProject != null && !localProject.needsSync) {
                    projectDao.insertProject(remoteProject.toEntity())
                }
            }

            val deletedProjectIds = localProjectIds - remoteProjectIds
            for (projectId in deletedProjectIds) {
                val localProject = localProjects.find { it.id == projectId }
                if (localProject?.needsSync != true) {
                    projectDao.deleteProject(projectId)
                    Log.d(TAG, "Deleted project locally: $projectId")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync projects", e)
        }
    }

    private suspend fun syncTasks() {
        try {
            val projects = projectDao.getAllProjectsSync()

            for (project in projects) {
                syncTasksForProject(project.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync tasks", e)
        }
    }

    private suspend fun syncTasksForProject(projectId: String) {
        try {
            val tasksNeedingSync =
                taskDao.getTasksNeedingSync().filter { it.projectId == projectId }
            for (task in tasksNeedingSync) {
                try {
                    when (SyncAction.valueOf(task.syncAction)) {
                        SyncAction.CREATE -> {
                            taskRemoteDataSource.addTaskToProject(projectId, task.toDomain())
                            taskDao.markTaskAsSynced(task.id)
                            Log.d(TAG, "Created task: ${task.id}")
                        }

                        SyncAction.UPDATE -> {
                            taskRemoteDataSource.updateTaskInProject(projectId, task.toDomain())
                            taskDao.markTaskAsSynced(task.id)
                            Log.d(TAG, "Updated task: ${task.id}")
                        }

                        SyncAction.DELETE -> {
                            taskRemoteDataSource.deleteTaskFromProject(projectId, task.id)
                            taskDao.deleteTask(task.id)
                            Log.d(TAG, "Deleted task: ${task.id}")
                        }

                        SyncAction.NONE -> {
                            taskDao.markTaskAsSynced(task.id)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync task ${task.id}", e)
                }
            }

            val remoteTasks = taskRemoteDataSource.getTasksForProject(projectId)
            val localTasks = taskDao.getTasksForProjectSync(projectId)

            val remoteTaskIds = remoteTasks.map { it.id }.toSet()
            val localTaskIds = localTasks.map { it.id }.toSet()

            val newTasks = remoteTasks.filter { !localTaskIds.contains(it.id) }
            if (newTasks.isNotEmpty()) {
                taskDao.insertTasks(newTasks.toEntityList())
                Log.d(TAG, "Inserted ${newTasks.size} new tasks for project $projectId")
            }

            for (remoteTask in remoteTasks) {
                val localTask = localTasks.find { it.id == remoteTask.id }
                if (localTask != null && !localTask.needsSync) {
                    taskDao.insertTask(remoteTask.toEntity())
                }
            }

            val deletedTaskIds = localTaskIds - remoteTaskIds
            for (taskId in deletedTaskIds) {
                val localTask = localTasks.find { it.id == taskId }
                if (localTask?.needsSync != true) {
                    taskDao.deleteTask(taskId)
                    Log.d(TAG, "Deleted task locally: $taskId")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync tasks for project $projectId", e)
        }
    }

    suspend fun forceSyncProject(projectId: String) {
        if (!connectivityObserver.isOnline()) return

        try {
            syncTasksForProject(projectId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force sync project $projectId", e)
        }
    }
}