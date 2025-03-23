package kv.apps.taskmanager.domain.usecase.projectsUseCases

import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.repository.ProjectRepository
import javax.inject.Inject

class ProjectUseCases @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend fun getAllProjectsForUser() = repository.getAllProjectsForUser()

    suspend fun getAllProjects() = repository.getAllProjects()

    suspend fun createProject(project: Project) = repository.createProject(project)

    suspend fun updateProject(projectId: String, project: Project) = repository.updateProject(projectId, project)

    suspend fun deleteProject(projectId: String) = repository.deleteProject(projectId)

    suspend fun getProjectById(projectId: String) = repository.getProjectById(projectId)

    suspend fun addTeamMembersToProject(projectId: String, teamMembers: List<String>) = repository.addTeamMembersToProject(projectId, teamMembers)

    suspend fun removeTeamMembersFromProject(projectId: String, teamMembersIds: List<String>) = repository.removeTeamMembersFromProject(projectId,
        teamMembersIds.toString()
    )

    suspend fun getTeamMembersForProject(projectId: String) = repository.getTeamMembersForProject(projectId)

    suspend fun sendProjectInvitation(invitation: ProjectInvitation) = repository.sendProjectInvitation(invitation)

    suspend fun getPendingProjectInvitations(toUserId: String) = repository.getPendingProjectInvitations(toUserId)

    suspend fun acceptInvitation(invitationId: String, projectId: String, userId: String) = repository.acceptInvitation(invitationId, projectId, userId)

    suspend fun rejectInvitation(invitationId: String, projectId: String, userId: String) = repository.rejectInvitation(invitationId, projectId, userId)
}