package kv.apps.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _sendProjectInvitationState = MutableStateFlow<Result<Unit>?>(null)
    val sendProjectInvitationState: StateFlow<Result<Unit>?> = _sendProjectInvitationState

    fun fetchAllProjects() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _projects.value = projectUseCases.getAllProjects()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch projects: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun getProjectById(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _selectedProject.value = projectUseCases.getProjectById(projectId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to fetch project: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createProject(project: Project) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.createProject(project)
                fetchAllProjects()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to create project: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProject(projectId: String, project: Project) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.updateProject(projectId, project)
                fetchAllProjects()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update project: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.deleteProject(projectId)
                fetchAllProjects()
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete project: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectProject(project: Project) {
        _selectedProject.value = project
    }

    fun addTeamMembersToProject(projectId: String, teamMembers: List<String>) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.addTeamMembersToProject(projectId, teamMembers)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add team members: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Remove team members from a project
    fun removeTeamMembersFromProject(projectId: String, teamMembersIds: List<String>) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.removeTeamMembersFromProject(projectId, teamMembersIds)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to remove team members: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchTeamMembersForProject(projectId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = projectUseCases.getTeamMembersForProject(projectId)
                result.fold(
                    onSuccess = { members ->
                        _teamMembers.value = members
                        _error.value = null
                    },
                    onFailure = { error ->
                        _error.value = "Failed to fetch team members: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Failed to fetch team members: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
    fun sendProjectInvitation(invitation: ProjectInvitation) {
        viewModelScope.launch {
            val result = projectUseCases.sendProjectInvitation(invitation)
            _sendProjectInvitationState.value = result
        }
    }
    fun clearSendProjectInvitationState() {
        _sendProjectInvitationState.value = null
    }
    fun getPendingProjectInvitations(toUserId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val result = projectUseCases.getPendingProjectInvitations(toUserId)
                result.fold(
                    onSuccess = { invitations ->
                        _error.value = null
                    },
                    onFailure = { error ->
                        _error.value = "Failed to fetch pending project invitations: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Failed to fetch pending project invitations: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun acceptInvitation(invitationId: String, projectId: String, userId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.acceptInvitation(invitationId, projectId, userId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to accept invitation: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun rejectInvitation(invitationId: String, projectId: String, userId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                projectUseCases.rejectInvitation(invitationId, projectId, userId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to reject invitation: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}