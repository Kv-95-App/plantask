package kv.apps.taskmanager.presentation.viewmodel.project

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.domain.usecase.projectsUseCases.ProjectUseCases
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectUseCases: ProjectUseCases,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProjectEvent>()
    val events: SharedFlow<ProjectEvent> = _events.asSharedFlow()

    private val invitationsCache = mutableStateMapOf<String, List<ProjectInvitation>>()
    val creatorNamesCache = mutableStateMapOf<String, Pair<String, String>>()
    val projectTitlesCache = mutableStateMapOf<String, String>()
    private val teamMembersCache = mutableStateMapOf<String, List<TeamMember>>()

    private suspend fun emitError(type: ProjectErrorType, message: String) {
        _events.emit(ProjectEvent.Error(type, message))
    }

    private suspend fun emitEvent(event: ProjectEvent) {
        _events.emit(event)
    }

    fun fetchTeamMembersForProject(projectId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && teamMembersCache[projectId] != null) {
            val cached = teamMembersCache[projectId]!!
            _uiState.update {
                it.copy(
                    teamMembersWithDetails = cached,
                    teamMemberIds = cached.map { m -> m.userId },
                    isTeamMembersLoading = false
                )
            }
            return
        }

        _uiState.update { it.copy(isTeamMembersLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val result = projectUseCases.getTeamMembersForProject(projectId)
                teamMembersCache[projectId] = result
                _uiState.update { state ->
                    state.copy(
                        teamMembersWithDetails = result,
                        teamMemberIds = result.map { it.userId },
                        isTeamMembersLoading = false
                    )
                }
                emitEvent(ProjectEvent.TeamMembersLoaded)
            } catch (e: Exception) {
                val msg = "Failed to load team members: ${e.message}"
                _uiState.update { it.copy(isTeamMembersLoading = false, errorMessage = msg) }
                emitError(ProjectErrorType.MEMBERS_ERROR, msg)
            }
        }
    }

    fun fetchAllProjects() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                projectUseCases.getAllProjectsForUser()
                    .onSuccess { projects ->
                        _uiState.update { it.copy(
                            projects = projects,
                            isLoading = false,
                            errorMessage = null
                        ) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            errorMessage = "Failed to fetch projects: ${e.message}"
                        ) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Unexpected error: ${e.message}"
                ) }
            }
        }
    }

    fun getProjectById(projectId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null, selectedProject = null) }
        viewModelScope.launch {
            try {
                projectUseCases.getProjectById(projectId)
                    .onSuccess { project ->
                        _uiState.update { state ->
                            state.copy(
                                selectedProject = project,
                                isLoading = false,
                                errorMessage = null
                            )
                        }
                        loadOwnerDetails(project.createdBy)
                        fetchTeamMembersForProject(projectId)
                        emitEvent(ProjectEvent.ProjectFetched)
                    }
                    .onFailure { e ->
                        val msg = "Failed to fetch project: ${e.message}"
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = msg,
                                selectedProject = null
                            )
                        }
                        emitError(ProjectErrorType.FETCH_ERROR, msg)
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = msg,
                        selectedProject = null
                    )
                }
                emitError(ProjectErrorType.FETCH_ERROR, msg)
            }
        }
    }

    fun createProject(project: Project) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val tempProjectId = project.id.ifEmpty { "temp-${System.currentTimeMillis()}" }
                val newList = uiState.value.projects + project.copy(id = tempProjectId)
                _uiState.update { it.copy(projects = newList) }

                projectUseCases.createProject(project)
                    .onSuccess { actualId ->
                        val finalProject = project.copy(id = actualId)
                        val updated = uiState.value.projects.map {
                            if (it.id == tempProjectId) finalProject else it
                        }
                        _uiState.update { it.copy(projects = updated, isLoading = false) }
                        emitEvent(ProjectEvent.ProjectCreated)

                        val membersToInvite = finalProject.teamMembers.filter {
                            it != finalProject.createdBy
                        }
                        if (membersToInvite.isNotEmpty()) {
                            membersToInvite.forEach { memberId ->
                                val invitation = ProjectInvitation(
                                    invitationId = "inv_${memberId}_${System.currentTimeMillis()}",
                                    fromUserId = finalProject.createdBy,
                                    toUserId = memberId,
                                    projectId = actualId,
                                    status = "Pending"
                                )
                                sendProjectInvitation(invitation)
                            }
                        }

                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        val msg = "Failed to create project: ${e.message}"
                        val reverted = uiState.value.projects.filter { it.id != tempProjectId }
                        _uiState.update {
                            it.copy(
                                projects = reverted,
                                isLoading = false,
                                errorMessage = msg
                            )
                        }
                        emitError(ProjectErrorType.CREATE_ERROR, msg)
                        fetchAllProjects()
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                emitError(ProjectErrorType.CREATE_ERROR, msg)
                fetchAllProjects()
            }
        }
    }

    fun updateProject(projectId: String, updatedProject: Project) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val updatedProjects = uiState.value.projects.map { project ->
                    if (project.id == projectId) updatedProject else project
                }
                _uiState.update { it.copy(projects = updatedProjects) }

                projectUseCases.updateProject(projectId, updatedProject)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        emitEvent(ProjectEvent.ProjectUpdated)
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        val msg = "Failed to update project: ${e.message}"
                        _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                        emitError(ProjectErrorType.UPDATE_ERROR, msg)
                        fetchAllProjects()
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                emitError(ProjectErrorType.UPDATE_ERROR, msg)
                fetchAllProjects()
            }
        }
    }

    fun deleteProject(projectId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val filteredProjects = uiState.value.projects.filter { it.id != projectId }
                _uiState.update { it.copy(projects = filteredProjects) }

                projectUseCases.deleteProject(projectId)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        emitEvent(ProjectEvent.ProjectDeleted)
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        val msg = "Failed to delete project: ${e.message}"
                        _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                        emitError(ProjectErrorType.DELETE_ERROR, msg)
                        fetchAllProjects()
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                emitError(ProjectErrorType.DELETE_ERROR, msg)
                fetchAllProjects()
            }
        }
    }

    fun isCurrentUserCreator(projectId: String): Boolean {
        val project = uiState.value.projects.find { it.id == projectId }
        return project?.createdBy == auth.currentUser?.uid
    }

    fun removeTeamMembersFromProject(projectId: String, teamMemberId: String, onSuccess: () -> Unit = {}) {
        if (!isCurrentUserCreator(projectId)) {
            val msg = "Only project creator can remove members"
            _uiState.update { it.copy(errorMessage = msg) }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                projectUseCases.removeTeamMembersFromProject(projectId, teamMemberId)
                    .onSuccess {
                        teamMembersCache.remove(projectId)
                        fetchTeamMembersForProject(projectId, true)
                        _uiState.update { it.copy(isLoading = false) }
                        onSuccess()
                    }
                    .onFailure { e ->
                        val msg = "Failed to remove team member: ${e.message}"
                        _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                        fetchTeamMembersForProject(projectId, true)
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                fetchTeamMembersForProject(projectId, true)
            }
        }
    }

    fun loadOwnerDetails(userId: String) {
        if (creatorNamesCache.containsKey(userId)) {
            val names = creatorNamesCache[userId]
            _uiState.update { it.copy(ownerDetails = names) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            projectUseCases.getProjectCreatorDetails(userId)
                .onSuccess { names ->
                    creatorNamesCache[userId] = names
                    _uiState.update { it.copy(ownerDetails = names, isLoading = false) }
                    emitEvent(ProjectEvent.OwnerDetailsLoaded)
                }
                .onFailure { e ->
                    val msg = "Failed to load owner details: ${e.message}"
                    _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                    emitError(ProjectErrorType.OWNER_ERROR, msg)
                }
        }
    }

    fun fetchCreatorName(userId: String) {
        if (creatorNamesCache.containsKey(userId)) return
        viewModelScope.launch {
            projectUseCases.getProjectCreatorDetails(userId)
                .onSuccess { names ->
                    creatorNamesCache[userId] = names
                }
                .onFailure { e ->
                }
        }
    }

    fun fetchProjectTitle(projectId: String) {
        if (projectTitlesCache.containsKey(projectId)) return
        viewModelScope.launch {
            projectUseCases.getProjectById(projectId)
                .onSuccess { project ->
                    projectTitlesCache[projectId] = project.title
                }
                .onFailure { e ->

                }
        }
    }

    fun sendProjectInvitation(invitation: ProjectInvitation) {
        if (invitation.fromUserId == invitation.toUserId) {
            _uiState.update { it.copy(errorMessage = "Cannot invite yourself") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val tempInvitation = invitation.copy(
                    invitationId = invitation.invitationId.ifEmpty { "temp-${System.currentTimeMillis()}" }
                )
                val updatedInvitations = uiState.value.invitations + tempInvitation
                _uiState.update { it.copy(invitations = updatedInvitations) }

                projectUseCases.sendProjectInvitation(invitation)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                        emitEvent(ProjectEvent.InvitationSent)

                        invitationsCache.remove(invitation.fromUserId)
                        invitationsCache.remove(invitation.toUserId)

                        if (invitation.toUserId == auth.currentUser?.uid) {
                            getPendingProjectInvitations(invitation.toUserId, true)
                        }
                    }
                    .onFailure { e ->
                        val updated = uiState.value.invitations.filter {
                            it.invitationId != tempInvitation.invitationId
                        }
                        _uiState.update {
                            it.copy(
                                invitations = updated,
                                isLoading = false
                            )
                        }

                        val msg = if (e.message?.contains("already has a pending invitation") == true) {
                            "Invitation already sent to this user"
                        } else {
                            "Failed to send invitation: ${e.message}"
                        }
                        _uiState.update { it.copy(errorMessage = msg) }
                        emitError(ProjectErrorType.INVITATION_ERROR, msg)
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                emitError(ProjectErrorType.INVITATION_ERROR, msg)
            }
        }
    }

    fun getPendingProjectInvitations(userId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && invitationsCache.containsKey(userId)) {
            val cached = invitationsCache[userId] ?: emptyList()
            _uiState.update {
                it.copy(
                    invitations = cached,
                    isNotificationsLoading = false
                )
            }
            viewModelScope.launch { emitEvent(ProjectEvent.InvitationsLoaded) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isNotificationsLoading = true) }
            try {
                projectUseCases.getPendingProjectInvitations(userId)
                    .onSuccess { invitations ->
                        val filtered = invitations.filter { it.toUserId == userId }
                        invitationsCache[userId] = filtered
                        _uiState.update {
                            it.copy(
                                invitations = filtered,
                                isNotificationsLoading = false
                            )
                        }
                        emitEvent(ProjectEvent.InvitationsLoaded)
                    }
                    .onFailure { e ->
                        val msg = e.message ?: "Failed to load invitations"
                        invitationsCache.remove(userId)
                        _uiState.update {
                            it.copy(
                                invitations = emptyList(),
                                isNotificationsLoading = false,
                                errorMessage = msg
                            )
                        }
                        emitError(ProjectErrorType.NOTIFICATION_ERROR, msg)
                    }
            } catch (e: Exception) {
                val msg = "Unexpected error: ${e.message}"
                invitationsCache.remove(userId)
                _uiState.update {
                    it.copy(
                        invitations = emptyList(),
                        isNotificationsLoading = false,
                        errorMessage = msg
                    )
                }
                emitError(ProjectErrorType.NOTIFICATION_ERROR, msg)
            }
        }
    }

    fun acceptInvitation(invitationId: String, projectId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isNotificationsLoading = true) }
            try {
                projectUseCases.acceptInvitation(invitationId, projectId, userId)
                    .onSuccess {
                        removeInvitation(invitationId)
                        _uiState.update {
                            it.copy(
                                invitationActionState = Result.success(Unit),
                                isNotificationsLoading = false
                            )
                        }
                        emitEvent(ProjectEvent.InvitationAccepted)
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                invitationActionState = Result.failure(e),
                                isNotificationsLoading = false,
                                errorMessage = "Failed to accept invitation: ${e.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        invitationActionState = Result.failure(e),
                        isNotificationsLoading = false,
                        errorMessage = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun rejectInvitation(invitationId: String, projectId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isNotificationsLoading = true) }
            try {
                projectUseCases.rejectInvitation(invitationId, projectId, userId)
                    .onSuccess {
                        removeInvitation(invitationId)
                        _uiState.update {
                            it.copy(
                                invitationActionState = Result.success(Unit),
                                isNotificationsLoading = false
                            )
                        }
                        emitEvent(ProjectEvent.InvitationRejected)
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                invitationActionState = Result.failure(e),
                                isNotificationsLoading = false,
                                errorMessage = "Failed to reject invitation: ${e.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        invitationActionState = Result.failure(e),
                        isNotificationsLoading = false,
                        errorMessage = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearInvitationActionState() {
        _uiState.update { it.copy(invitationActionState = null) }
    }

    private fun removeInvitation(invitationId: String) {
        val updated = uiState.value.invitations.filter { it.invitationId != invitationId }
        _uiState.update { it.copy(invitations = updated) }

        auth.currentUser?.uid?.let { userId ->
            invitationsCache[userId] = invitationsCache[userId]?.filter {
                it.invitationId != invitationId
            } ?: emptyList()
        }
    }

    fun clearNotificationsCache() {
        invitationsCache.clear()
        _uiState.update { it.copy(invitations = emptyList()) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}