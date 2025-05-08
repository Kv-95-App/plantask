package kv.apps.taskmanager.data.repositoryImpl

import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.repository.ProjectRepository
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectRemoteDataSource: ProjectRemoteDataSource
) : ProjectRepository {

    override suspend fun getAllProjectsForUser(): Result<List<Project>> = runCatching {
        projectRemoteDataSource.getAllProjectsForUser()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("Failed to get user projects: ${it.message}")) }
    )


    override suspend fun createProject(project: Project): Result<String> = runCatching {
        projectRemoteDataSource.createProject(project)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("Failed to create project: ${it.message}")) }
    )

    override suspend fun deleteProject(projectId: String): Result<Unit> = runCatching {
        projectRemoteDataSource.deleteProject(projectId)
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(Exception("Failed to delete project: ${it.message}")) }
    )

    override suspend fun updateProject(projectId: String, project: Project): Result<Unit> = runCatching {
        projectRemoteDataSource.updateProject(projectId, project)
    }.fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { Result.failure(Exception("Failed to update project: ${it.message}")) }
    )

    override suspend fun getProjectById(projectId: String): Result<Project> = runCatching {
        projectRemoteDataSource.getProjectById(projectId) ?: throw Exception("Project not found")
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(Exception("Failed to get project: ${it.message}")) }
    )

    override suspend fun addTeamMembersToProject(
        projectId: String,
        teamMemberIds: List<String>
    ): Result<Unit> {
        return projectRemoteDataSource.addTeamMembersToProject(projectId, teamMemberIds)
    }

    override suspend fun removeTeamMembersFromProject(
        projectId: String,
        teamMemberId: String
    ): Result<Unit> {
        return projectRemoteDataSource.removeTeamMembersFromProject(projectId, teamMemberId)
    }

    override suspend fun getTeamMembersForProject(projectId: String): Result<List<String>> {
        return projectRemoteDataSource.getTeamMembersForProject(projectId)
    }

    override suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit> {
        return try {
            projectRemoteDataSource.sendProjectInvitation(invitation)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to send invitation: ${e.message}"))
        }
    }

    override suspend fun getPendingProjectInvitations(userId: String): Result<List<ProjectInvitation>> {
        return try {
            val invitations = projectRemoteDataSource.getPendingProjectInvitations(userId)
            Result.success(invitations)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get invitations: ${e.message}"))
        }
    }

    override suspend fun acceptInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> {
        return try {
            projectRemoteDataSource.acceptInvitation(invitationId, projectId, userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to accept invitation: ${e.message}"))
        }
    }

    override suspend fun rejectInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> {
        return try {
            projectRemoteDataSource.rejectInvitation(invitationId, projectId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reject invitation: ${e.message}"))
        }
    }
    override suspend fun getProjectCreatorDetails(createdById: String): Result<Pair<String, String>> {
        return try {
            projectRemoteDataSource.getProjectCreatorDetails(createdById)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get creator details: ${e.message}"))
        }
    }
}
