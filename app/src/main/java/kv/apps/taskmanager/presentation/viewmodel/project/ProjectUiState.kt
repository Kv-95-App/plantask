package kv.apps.taskmanager.presentation.viewmodel.project

import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.TeamMember

data class ProjectUiState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val teamMemberIds: List<String> = emptyList(),
    val teamMembersWithDetails: List<TeamMember> = emptyList(),
    val isTeamMembersLoading: Boolean = false,
    val teamMembersError: String? = null,

    val invitations: List<ProjectInvitation> = emptyList(),
    val isNotificationsLoading: Boolean = false,
    val invitationsError: String? = null,
    val invitationActionState: Result<Unit>? = null,

    val ownerDetails: Pair<String, String>? = null,

    val isRefreshing: Boolean = false,
    val actionInProgress: Boolean = false,
    val selectedProjectId: String? = null
)