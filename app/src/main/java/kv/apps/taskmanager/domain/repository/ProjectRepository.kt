package kv.apps.taskmanager.domain.repository

import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation

interface ProjectRepository {
    suspend fun getAllProjectsForUser(): List<Project>
    suspend fun getAllProjects(): List<Project>
    suspend fun createProject(project: Project): String
    suspend fun deleteProject(projectId: String)
    suspend fun updateProject(projectId: String, project: Project)
    suspend fun getProjectById(projectId: String): Project?
    suspend fun addTeamMembersToProject(projectId: String, teamMemberIds: List<String>): Result<Unit>
    suspend fun removeTeamMembersFromProject(projectId: String, teamMemberId: String): Result<Unit>
    suspend fun getTeamMembersForProject(projectId: String): Result<List<String>>
    suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit>
    suspend fun getPendingProjectInvitations(toUserId: String): Result<List<ProjectInvitation>>
    suspend fun acceptInvitation(invitationId: String, projectId: String, userId: String): Result<Unit>
    suspend fun rejectInvitation(invitationId: String, projectId: String, userId: String): Result<Unit>
}