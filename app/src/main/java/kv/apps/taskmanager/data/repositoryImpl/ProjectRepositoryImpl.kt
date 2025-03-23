package kv.apps.taskmanager.data.repositoryImpl

import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.repository.ProjectRepository
import javax.inject.Inject

class ProjectRepositoryImpl @Inject constructor(
    private val projectRemoteDataSource: ProjectRemoteDataSource
) : ProjectRepository {

    override suspend fun getAllProjectsForUser(): List<Project> {
        return projectRemoteDataSource.getAllProjectsForUser()
    }

    override suspend fun getAllProjects(): List<Project> {
        return projectRemoteDataSource.getAllProjects()
    }

    override suspend fun createProject(project: Project): String {
        return projectRemoteDataSource.createProject(project)
    }

    override suspend fun deleteProject(projectId: String) {
        projectRemoteDataSource.deleteProject(projectId)
    }

    override suspend fun updateProject(projectId: String, project: Project) {
        projectRemoteDataSource.updateProject(projectId, project)
    }

    override suspend fun getProjectById(projectId: String): Project? {
        return projectRemoteDataSource.getProjectById(projectId)
    }

    override suspend fun addTeamMembersToProject(projectId: String, teamMemberIds: List<String>): Result<Unit> {
        return projectRemoteDataSource.addTeamMembersToProject(projectId, teamMemberIds)
    }

    override suspend fun removeTeamMembersFromProject(projectId: String, teamMemberId: String): Result<Unit> {
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
            Result.failure(Exception("Failed to send project invitation: ${e.message}"))
        }
    }

    override suspend fun getPendingProjectInvitations(toUserId: String): Result<List<ProjectInvitation>> {
        return try {
            val invitations = projectRemoteDataSource.getPendingProjectInvitations(toUserId)
            Result.success(invitations)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch pending project invitations: ${e.message}"))
        }
    }

    override suspend fun acceptInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> {
        return projectRemoteDataSource.acceptInvitation(invitationId, projectId, userId)
    }

    override suspend fun rejectInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> {
        return projectRemoteDataSource.rejectInvitation(invitationId, projectId, userId)
    }
}