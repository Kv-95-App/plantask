package kv.apps.taskmanager.presentation.viewmodel.project

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectUseCases: ProjectUseCases
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

    private val _invitations = MutableStateFlow<List<ProjectInvitation>>(emptyList())
    val invitations: StateFlow<List<ProjectInvitation>> = _invitations.asStateFlow()

    private val _invitationActionState = MutableStateFlow<Result<Unit>?>(null)
    val invitationActionState: StateFlow<Result<Unit>?> = _invitationActionState.asStateFlow()

    private val _creatorNamesCache = mutableStateMapOf<String, Pair<String, String>>()
    val creatorNamesCache: Map<String, Pair<String, String>> get() = _creatorNamesCache

    private val _projectTitlesCache = mutableStateMapOf<String, String>()
    val projectTitlesCache: Map<String, String> get() = _projectTitlesCache

    private val _teamMembersCache = mutableStateMapOf<String, List<TeamMember>>()

    private val _ownerDetails = MutableStateFlow<Pair<String, String>?>(null)
    val ownerDetails: StateFlow<Pair<String, String>?> = _ownerDetails.asStateFlow()

    private val _memberDetails = MutableStateFlow<Pair<String, String>?>(null)
    val memberDetails: StateFlow<Pair<String, String>?> = _memberDetails.asStateFlow()

    private val _projectTeamMembers = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val projectTeamMembers: StateFlow<Map<String, List<String>>> = _projectTeamMembers.asStateFlow()

    fun fetchTeamMembersForProject(projectId: String) {
        _teamMembersCache[projectId]?.let { cachedMembers ->
            _teamMembersWithDetails.value = cachedMembers
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


    fun removeTeamMembersFromProject(projectId: String, teamMemberId: String) {
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
                        fetchTeamMembersForProject(projectId)
                        _error.value = null
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        _error.value = "Failed to remove team member: ${e.message}"
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
                        getPendingProjectInvitations(invitation.toUserId)
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

    fun getPendingProjectInvitations(userId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.getPendingProjectInvitations(userId)
                .onSuccess { invitations ->
                    _invitations.value = invitations
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to fetch invitations: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun acceptInvitation(invitationId: String, projectId: String, userId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.acceptInvitation(invitationId, projectId, userId)
                .onSuccess {
                    getPendingProjectInvitations(userId)
                    _invitationActionState.value = Result.success(Unit)
                }
                .onFailure { e ->
                    _invitationActionState.value = Result.failure(e)
                }
            _loading.value = false
        }
    }

    fun rejectInvitation(invitationId: String, projectId: String, userId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.rejectInvitation(invitationId, projectId, userId)
                .onSuccess {
                    getPendingProjectInvitations(userId)
                    _invitationActionState.value = Result.success(Unit)
                }
                .onFailure { e ->
                    _invitationActionState.value = Result.failure(e)
                }
            _loading.value = false
        }
    }

    fun clearInvitationActionState() {
        _invitationActionState.value = null
    }

    fun clearError() {
        _error.value = null
    }
}