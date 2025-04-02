package kv.apps.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
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

    fun fetchAllProjects() {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.getAllProjects()
                .onSuccess { projects ->
                    _projects.value = projects
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to fetch projects: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun getProjectById(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.getProjectById(projectId)
                .onSuccess { project ->
                    _selectedProject.value = project
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to fetch project: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun createProject(project: Project) {
        viewModelScope.launch {
            _loading.value = true
            projectUseCases.createProject(project)
                .onSuccess {
                    delay(500)
                    fetchAllProjects()
                }
                .onFailure { e ->
                    _error.value = "Failed to create project: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun updateProject(projectId: String, project: Project) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.updateProject(projectId, project)
                .onSuccess {
                    fetchAllProjects()
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to update project: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun deleteProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.deleteProject(projectId)
                .onSuccess {
                    fetchAllProjects()
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to delete project: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun addTeamMembersToProject(projectId: String, teamMembers: List<String>) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.addTeamMembersToProject(projectId, teamMembers)
                .onSuccess {
                    fetchTeamMembersForProject(projectId)
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to add team members: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun removeTeamMembersFromProject(projectId: String, teamMemberId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.removeTeamMembersFromProject(projectId, teamMemberId)
                .onSuccess {
                    fetchTeamMembersForProject(projectId)
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to remove team member: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun fetchTeamMembersForProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            projectUseCases.getTeamMembersForProject(projectId)
                .onSuccess { members ->
                    _teamMembers.value = members
                    _error.value = null
                }
                .onFailure { e ->
                    _error.value = "Failed to fetch team members: ${e.message}"
                }
            _loading.value = false
        }
    }

    fun sendProjectInvitation(invitation: ProjectInvitation) {
        viewModelScope.launch {
            _loading.value = true
            projectUseCases.sendProjectInvitation(invitation)
                .onSuccess {
                    _invitationActionState.value = Result.success(Unit)
                }
                .onFailure { e ->
                    _invitationActionState.value = Result.failure(e)
                }
            _loading.value = false
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