package kv.apps.taskmanager.data.repositoryImpl

import kv.apps.taskmanager.data.connectivity.ConnectivityObserver
import kv.apps.taskmanager.data.local.dao.ProjectDao
import kv.apps.taskmanager.data.local.entity.SyncAction
import kv.apps.taskmanager.data.local.mapper.toDomain
import kv.apps.taskmanager.data.local.mapper.toDomainList
import kv.apps.taskmanager.data.local.mapper.toEntity
import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.data.sync.SyncManager
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.domain.repository.ProjectRepository
import java.util.UUID
import javax.inject.Inject

class OfflineFirstProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
    private val projectRemoteDataSource: ProjectRemoteDataSource,
    private val connectivityObserver: ConnectivityObserver,
    private val syncManager: SyncManager
) : ProjectRepository {

    override suspend fun getAllProjectsForUser(): Result<List<Project>> = runCatching {
        if (connectivityObserver.isOnline()) {
            try {
                syncManager.syncAll()
            } catch (_: Exception) {
            }
        }

        projectDao.getAllProjectsSync().toDomainList()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("Failed to get user projects: ${it.message}")) }
    )

    override suspend fun createProject(project: Project): Result<String> = runCatching {
        val projectWithId = if (project.id.isEmpty()) {
            project.copy(id = UUID.randomUUID().toString())
        } else project

        val projectEntity = projectWithId.toEntity(
            needsSync = true,
            syncAction = SyncAction.CREATE
        )
        projectDao.insertProject(projectEntity)

        if (connectivityObserver.isOnline()) {
            try {
                val remoteProjectId = projectRemoteDataSource.createProject(projectWithId)
                if (remoteProjectId != projectWithId.id) {
                    val updatedProject = projectEntity.copy(
                        id = remoteProjectId,
                        needsSync = false,
                        syncAction = SyncAction.NONE.name
                    )
                    projectDao.deleteProject(projectWithId.id)
                    projectDao.insertProject(updatedProject)
                    remoteProjectId
                } else {
                    projectDao.markProjectAsSynced(projectWithId.id)
                    projectWithId.id
                }
            } catch (_: Exception) {
                projectWithId.id
            }
        } else {
            projectWithId.id
        }
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("Failed to create project: ${it.message}")) }
    )

    override suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
        projectDao.markProjectAsDeleted(projectId)

        if (connectivityObserver.isOnline()) {
            try {
                projectRemoteDataSource.deleteProject(projectId)
                projectDao.deleteProject(projectId)
            } catch (_: Exception) {
            }
        }
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(Exception("Failed to delete project: ${it.message}")) }
    )

    override suspend fun updateProject(projectId: String, project: Project): Result<Unit> =
        runCatching {
            val projectEntity = project.toEntity(
                needsSync = true,
                syncAction = SyncAction.UPDATE
            )
            projectDao.insertProject(projectEntity)

            if (connectivityObserver.isOnline()) {
                try {
                    projectRemoteDataSource.updateProject(projectId, project)
                    projectDao.markProjectAsSynced(projectId)
                } catch (_: Exception) {
                }
            }
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(Exception("Failed to update project: ${it.message}")) }
        )

    override suspend fun getProjectById(projectId: String): Result<Project> = runCatching {
        if (connectivityObserver.isOnline()) {
            try {
                val remoteProject = projectRemoteDataSource.getProjectById(projectId)
                if (remoteProject != null) {
                    projectDao.insertProject(remoteProject.toEntity())
                }
            } catch (_: Exception) {
            }
        }

        projectDao.getProjectById(projectId)?.toDomain()
            ?: throw Exception("Project not found")
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("Failed to get project: ${it.message}")) }
    )

    override suspend fun addTeamMembersToProject(
        projectId: String,
        teamMemberIds: List<String>
    ): Result<Unit> {
        return if (connectivityObserver.isOnline()) {
            projectRemoteDataSource.addTeamMembersToProject(projectId, teamMemberIds)
        } else {
            Result.failure(Exception("Adding team members requires internet connection"))
        }
    }

    override suspend fun removeTeamMembersFromProject(
        projectId: String,
        teamMemberId: String
    ): Result<Unit> {
        return if (connectivityObserver.isOnline()) {
            projectRemoteDataSource.removeTeamMembersFromProject(projectId, teamMemberId)
        } else {
            Result.failure(Exception("Removing team members requires internet connection"))
        }
    }

    override suspend fun getTeamMembersForProject(projectId: String): List<TeamMember> {
        return if (connectivityObserver.isOnline()) {
            try {
                projectRemoteDataSource.getTeamMembersForProject(projectId)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    override suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit> {
        return if (connectivityObserver.isOnline()) {
            try {
                projectRemoteDataSource.sendProjectInvitation(invitation)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to send invitation: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Sending invitations requires internet connection"))
        }
    }

    override suspend fun getPendingProjectInvitations(userId: String): Result<List<ProjectInvitation>> {
        return if (connectivityObserver.isOnline()) {
            try {
                val invitations = projectRemoteDataSource.getPendingProjectInvitations(userId)
                Result.success(invitations)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get invitations: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Getting invitations requires internet connection"))
        }
    }

    override suspend fun acceptInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> {
        return if (connectivityObserver.isOnline()) {
            try {
                projectRemoteDataSource.acceptInvitation(invitationId, projectId, userId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to accept invitation: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Accepting invitations requires internet connection"))
        }
    }

    override suspend fun rejectInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> {
        return if (connectivityObserver.isOnline()) {
            try {
                projectRemoteDataSource.rejectInvitation(invitationId, projectId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to reject invitation: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Rejecting invitations requires internet connection"))
        }
    }

    override suspend fun getProjectCreatorDetails(createdById: String): Result<Pair<String, String>> {
        return if (connectivityObserver.isOnline()) {
            try {
                projectRemoteDataSource.getProjectCreatorDetails(createdById)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to get creator details: ${e.message}"))
            }
        } else {
            Result.failure(Exception("Getting creator details requires internet connection"))
        }
    }
}