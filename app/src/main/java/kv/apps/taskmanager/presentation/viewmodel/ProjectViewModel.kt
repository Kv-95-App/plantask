package kv.apps.taskmanager.presentation.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.repository.ProjectRepository
import kv.apps.taskmanager.domain.usecase.projectsUseCases.ProjectUseCases
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val projectUseCases: ProjectUseCases
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject

    private val _teamMembers = MutableStateFlow<List<String>>(emptyList())
    val teamMembers: StateFlow<List<String>> = _teamMembers

    private val _invitations = MutableStateFlow<List<ProjectInvitation>>(emptyList())
    val invitations: StateFlow<List<ProjectInvitation>> = _invitations

    private val _invitationActionState = MutableStateFlow<Result<Unit>?>(null)
    val invitationActionState: StateFlow<Result<Unit>?> = _invitationActionState

    private val _creatorNamesCache = mutableStateMapOf<String, Pair<String, String>>()
    val creatorNamesCache: Map<String, Pair<String, String>> get() = _creatorNamesCache

    private val _projectTitlesCache = mutableStateMapOf<String, String>()
    val projectTitlesCache: Map<String, String> get() = _projectTitlesCache

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
                val newProjectId = project.id.ifEmpty { "temp-${System.currentTimeMillis()}" }
                _projects.value = _projects.value + project.copy(id = newProjectId)

                projectUseCases.createProject(project)
                    .onSuccess { actualId ->
                        _projects.value = _projects.value.map {
                            if (it.id == newProjectId) it.copy(id = actualId) else it
                        }
                        _error.value = null
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        _error.value = "Failed to create project: ${e.message}"
                        _projects.value = _projects.value.filter { it.id != newProjectId }
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

    fun addTeamMembersToProject(projectId: String, teamMembers: List<String>) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _projects.value = _projects.value.map { project ->
                    if (project.id == projectId) {
                        project.copy(teamMembers = project.teamMembers + teamMembers)
                    } else project
                }

                projectUseCases.addTeamMembersToProject(projectId, teamMembers)
                    .onSuccess {
                        fetchTeamMembersForProject(projectId)
                        _error.value = null
                        fetchAllProjects()
                    }
                    .onFailure { e ->
                        _error.value = "Failed to add team members: ${e.message}"
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

    fun fetchTeamMembersForProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.getTeamMembersForProject(projectId)
                    .onSuccess { members ->
                        _teamMembers.value = members
                        _error.value = null
                    }
                    .onFailure { e ->
                        _error.value = "Failed to fetch team members: ${e.message}"
                    }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _loading.value = false
            }
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

    fun selectProject(project: Project) {
        _selectedProject.value = project
    }

    fun clearInvitationActionState() {
        _invitationActionState.value = null
    }

    fun clearError() {
        _error.value = null
    }
}