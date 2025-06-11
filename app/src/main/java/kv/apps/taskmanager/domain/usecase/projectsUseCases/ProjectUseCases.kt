package kv.apps.taskmanager.domain.usecase.projectsUseCases

import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.domain.repository.ProjectRepository
import javax.inject.Inject

class ProjectUseCases @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend fun getAllProjectsForUser(): Result<List<Project>> =
        wrapRepositoryCall(
            repositoryCall = { repository.getAllProjectsForUser() },
            errorPrefix = "Failed to load user projects"
        )

    suspend fun createProject(project: Project): Result<String> =
        wrapRepositoryCall(
            repositoryCall = { repository.createProject(project) },
            errorPrefix = "Failed to create project"
        )

    suspend fun updateProject(projectId: String, project: Project): Result<Unit> =
        wrapRepositoryCall(
            repositoryCall = { repository.updateProject(projectId, project) },
            errorPrefix = "Failed to update project"
        )

    suspend fun deleteProject(projectId: String): Result<Unit> =
        wrapRepositoryCall(
            repositoryCall = { repository.deleteProject(projectId) },
            errorPrefix = "Failed to delete project"
        )

    suspend fun getProjectById(projectId: String): Result<Project> =
        wrapRepositoryCall(
            repositoryCall = { repository.getProjectById(projectId) },
            errorPrefix = "Failed to get project"
        )


    suspend fun removeTeamMembersFromProject(
        projectId: String,
        teamMemberId: String
    ): Result<Unit> = wrapRepositoryCall(
        repositoryCall = { repository.removeTeamMembersFromProject(projectId, teamMemberId) },
        errorPrefix = "Failed to remove team member"
    )

    suspend fun getTeamMembersForProject(projectId: String): List<TeamMember> =

        repository.getTeamMembersForProject(projectId)


    suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit> =
        wrapRepositoryCall(
            repositoryCall = { repository.sendProjectInvitation(invitation) },
            errorPrefix = "Failed to send invitation"
        )

    suspend fun getPendingProjectInvitations(toUserId: String): Result<List<ProjectInvitation>> =
        wrapRepositoryCall(
            repositoryCall = { repository.getPendingProjectInvitations(toUserId) },
            errorPrefix = "Failed to get pending invitations"
        )

    suspend fun acceptInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> = wrapRepositoryCall(
        repositoryCall = { repository.acceptInvitation(invitationId, projectId, userId) },
        errorPrefix = "Failed to accept invitation"
    )

    suspend fun rejectInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> = wrapRepositoryCall(
        repositoryCall = { repository.rejectInvitation(invitationId, projectId, userId) },
        errorPrefix = "Failed to reject invitation"
    )

    suspend fun getProjectCreatorDetails(createdById: String): Result<Pair<String, String>> =
        wrapRepositoryCall(
            repositoryCall = { repository.getProjectCreatorDetails(createdById) },
            errorPrefix = "Failed to get creator details"
        )

    private suspend fun <T> wrapRepositoryCall(
        repositoryCall: suspend () -> Result<T>,
        errorPrefix: String
    ): Result<T> {
        return try {
            repositoryCall().fold(
                onSuccess = { Result.success(it) },
                onFailure = { e -> Result.failure(Exception("$errorPrefix: ${e.message}")) }
            )
        } catch (e: Exception) {
            Result.failure(Exception("$errorPrefix: ${e.message}"))
        }
    }
}