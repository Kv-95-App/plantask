package kv.apps.taskmanager.presentation.viewmodel.project

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.domain.usecase.projectsUseCases.ProjectUseCases
import javax.inject.Inject
import kotlin.let

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectUseCases: ProjectUseCases,
    private val auth: FirebaseAuth

) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _teamMembers = MutableStateFlow<List<String>>(emptyList())
    val teamMembers: StateFlow<List<String>> = _teamMembers.asStateFlow()

    private val _teamMembersWithDetails = MutableStateFlow<List<TeamMember>>(emptyList())
    val teamMembersWithDetails: StateFlow<List<TeamMember>> = _teamMembersWithDetails.asStateFlow()

    private val _teamMembersLoading = MutableStateFlow(false)
    val teamMembersLoading: StateFlow<Boolean> = _teamMembersLoading.asStateFlow()

    private val _teamMembersError = MutableStateFlow<String?>(null)
    val teamMembersError: StateFlow<String?> = _teamMembersError.asStateFlow()

    private val _notificationsLoading = MutableStateFlow(false)
    val notificationsLoading: StateFlow<Boolean> = _notificationsLoading.asStateFlow()

    private val _invitations = MutableStateFlow<List<ProjectInvitation>>(emptyList())
    val invitations: StateFlow<List<ProjectInvitation>> = _invitations.asStateFlow()

    private val _invitationActionState = MutableStateFlow<Result<Unit>?>(null)
    val invitationActionState: StateFlow<Result<Unit>?> = _invitationActionState.asStateFlow()

    private val invitationsCache = mutableStateMapOf<String, List<ProjectInvitation>>()

    private val _creatorNamesCache = mutableStateMapOf<String, Pair<String, String>>()
    val creatorNamesCache: Map<String, Pair<String, String>> get() = _creatorNamesCache

    private val _projectTitlesCache = mutableStateMapOf<String, String>()
    val projectTitlesCache: Map<String, String> get() = _projectTitlesCache

    private val _teamMembersCache = mutableStateMapOf<String, List<TeamMember>>()

    private val _ownerDetails = MutableStateFlow<Pair<String, String>?>(null)


    fun fetchTeamMembersForProject(projectId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && _teamMembersCache[projectId] != null) {
            _teamMembersWithDetails.value = _teamMembersCache[projectId]!!
            return
        }

        _teamMembersLoading.value = true
        _teamMembersError.value = null

        viewModelScope.launch {
            try {
                val result = projectUseCases.getTeamMembersForProject(projectId)
                _teamMembersWithDetails.value = result
                _teamMembersCache[projectId] = result
                _teamMembers.value = result.map { it.userId }
            } catch (e: Exception) {
                _teamMembersError.value = "Failed to load team members: ${e.message}"
            } finally {
                _teamMembersLoading.value = false
            }
        }
    }

    fun fetchAllProjects() {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.getAllProjectsForUser()
                    .onSuccess { projects ->
                        _projects.value = projects
                        _error.value = null
                    }
                    .onFailure { e ->
                        _error.value = "Failed to fetch projects: ${e.message}"
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getProjectById(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.getProjectById(projectId)
                    .onSuccess { project ->
                        _selectedProject.value = project
                        loadOwnerDetails(project.createdBy)
                        fetchTeamMembersForProject(projectId)
                        _error.value = null
                    }
                    .onFailure { e ->
                        _error.value = "Failed to fetch project: ${e.message}"
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createProject(project: Project) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val tempProjectId = project.id.ifEmpty { "temp-${System.currentTimeMillis()}" }
                _projects.value = _projects.value + project.copy(id = tempProjectId)

                projectUseCases.createProject(project)
                    .onSuccess { actualId ->
                        _projects.value = _projects.value.map {
                            if (it.id == tempProjectId) it.copy(id = actualId) else it
                        }
                        _error.value = null
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        _error.value = "Failed to create project: ${e.message}"
                        _projects.value = _projects.value.filter { it.id != tempProjectId }
                        fetchAllProjects()
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                fetchAllProjects()
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProject(projectId: String, updatedProject: Project) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _projects.value = _projects.value.map { project ->
                    if (project.id == projectId) updatedProject else project
                }
                projectUseCases.updateProject(projectId, updatedProject)
                    .onSuccess {
                        _error.value = null
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        _error.value = "Failed to update project: ${e.message}"
                        fetchAllProjects()
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                fetchAllProjects()
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _projects.value = _projects.value.filter { it.id != projectId }
                projectUseCases.deleteProject(projectId)
                    .onSuccess {
                        _error.value = null
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        _error.value = "Failed to delete project: ${e.message}"
                        fetchAllProjects()
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                fetchAllProjects()
            } finally {
                _loading.value = false
            }
        }
    }
    fun isCurrentUserCreator(projectId: String): Boolean {
        val project = _projects.value.find { it.id == projectId }
        return project?.createdBy == auth.currentUser?.uid
    }


    fun removeTeamMembersFromProject(projectId: String, teamMemberId: String, onSuccess: () -> Int) {
        if (!isCurrentUserCreator(projectId)) {
            _error.value = "Only project creator can remove members"
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                _projects.value = _projects.value.map { project ->
                    if (project.id == projectId) {
                        project.copy(teamMembers = project.teamMembers - teamMemberId)
                    } else project
                }

                projectUseCases.removeTeamMembersFromProject(projectId, teamMemberId)
                    .onSuccess {
                        fetchTeamMembersForProject(projectId, true)
                        _error.value = null
                    }
                    .onFailure { e ->
                        _error.value = "Failed to remove team member: ${e.message}"
                        fetchTeamMembersForProject(projectId, true)
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                fetchTeamMembersForProject(projectId, true)
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadOwnerDetails(userId: String) {
        if (_creatorNamesCache.containsKey(userId)) {
            _ownerDetails.value = _creatorNamesCache[userId]
            return
        }

        viewModelScope.launch {
            _loading.value = true
            projectUseCases.getProjectCreatorDetails(userId)
                .onSuccess { names ->
                    _creatorNamesCache[userId] = names
                    _ownerDetails.value = names
                }
                .onFailure { e ->
                    _error.value = "Failed to load owner details: ${e.message}"
                }
            _loading.value = false
        }
    }


    fun fetchCreatorName(userId: String) {
        if (_creatorNamesCache.containsKey(userId)) return
        viewModelScope.launch {
            _loading.value = true
            projectUseCases.getProjectCreatorDetails(userId)
                .onSuccess { names ->
                    _creatorNamesCache[userId] = names
                }
                .onFailure { e ->
                }
            _loading.value = false
        }
    }

    fun fetchProjectTitle(projectId: String) {
        if (_projectTitlesCache.containsKey(projectId)) return
        viewModelScope.launch {
            _loading.value = true
            projectUseCases.getProjectById(projectId)
                .onSuccess { project ->
                    _projectTitlesCache[projectId] = project.title
                }
                .onFailure { e ->
                }
            _loading.value = false
        }
    }

    fun sendProjectInvitation(invitation: ProjectInvitation) {
        if (invitation.fromUserId == invitation.toUserId) {
            _error.value = "Cannot invite yourself"
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val tempInvitation = invitation.copy(
                    invitationId = invitation.invitationId.ifEmpty { "temp-${System.currentTimeMillis()}" }
                )
                _invitations.value = _invitations.value + tempInvitation

                projectUseCases.sendProjectInvitation(invitation)
                    .onSuccess {
                        _invitationActionState.value = Result.success(Unit)
                        _error.value = null
                        if (invitation.toUserId == auth.currentUser?.uid) {
                            getPendingProjectInvitations(invitation.toUserId, true)
                        }
                    }
                    .onFailure { e ->
                        _invitations.value = _invitations.value - tempInvitation
                        _invitationActionState.value = Result.failure(e)
                        _error.value = "Failed to send invitation: ${e.message}"
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    fun getPendingProjectInvitations(userId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && invitationsCache.containsKey(userId)) {
            _invitations.value = invitationsCache[userId] ?: emptyList()
            return
        }

        viewModelScope.launch {
            _notificationsLoading.value = true
            try {
                projectUseCases.getPendingProjectInvitations(userId)
                    .onSuccess { invitations ->
                        val filtered = invitations.filter { it.toUserId == userId }
                        _invitations.value = filtered
                        invitationsCache[userId] = filtered
                        _error.value = null
                    }
                    .onFailure { e ->
                        _error.value = e.message
                        _invitations.value = emptyList()
                        invitationsCache.remove(userId)
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                _invitations.value = emptyList()
                invitationsCache.remove(userId)
            } finally {
                _notificationsLoading.value = false
            }
        }
    }

    fun acceptInvitation(invitationId: String, projectId: String, userId: String) {
        viewModelScope.launch {
            _notificationsLoading.value = true
            try {
                projectUseCases.acceptInvitation(invitationId, projectId, userId)
                    .onSuccess {
                        removeInvitation(invitationId)
                        _invitationActionState.value = Result.success(Unit)
                    }
                    .onFailure { e ->
                        _invitationActionState.value = Result.failure(e)
                        _error.value = "Failed to accept invitation: ${e.message}"
                    }
            } finally {
                _notificationsLoading.value = false
            }
        }
    }

    fun rejectInvitation(invitationId: String, projectId: String, userId: String) {
        viewModelScope.launch {
            _notificationsLoading.value = true
            try {
                projectUseCases.rejectInvitation(
                    invitationId, projectId,
                    userId
                )
                    .onSuccess {
                        removeInvitation(invitationId)
                        _invitationActionState.value = Result.success(Unit)
                    }
                    .onFailure { e ->
                        _invitationActionState.value = Result.failure(e)
                        _error.value = "Failed to reject invitation: ${e.message}"
                    }
            } finally {
                _notificationsLoading.value = false
            }
        }
    }
    fun removeInvitation(invitationId: String) {
        _invitations.value = _invitations.value.filter { it.invitationId != invitationId }
        auth.currentUser?.uid?.let { userId ->
            invitationsCache[userId] = invitationsCache[userId]?.filter { it.invitationId != invitationId } as List<ProjectInvitation>
        }
    }

    fun clearInvitationActionState() {
        _invitationActionState.value = null
    }

    fun clearNotificationsCache() {
        invitationsCache.clear()
        _invitations.value = emptyList()
    }


    fun clearError() {
        _error.value = null
    }
}