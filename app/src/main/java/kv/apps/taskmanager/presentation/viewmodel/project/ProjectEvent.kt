package kv.apps.taskmanager.presentation.viewmodel.project

sealed class ProjectEvent {
    object ProjectsLoaded : ProjectEvent()
    object ProjectCreated : ProjectEvent()
    object ProjectUpdated : ProjectEvent()
    object ProjectDeleted : ProjectEvent()
    object ProjectFetched : ProjectEvent()

    object TeamMembersLoaded : ProjectEvent()

    object InvitationsLoaded : ProjectEvent()
    object InvitationSent : ProjectEvent()
    object InvitationAccepted : ProjectEvent()
    object InvitationRejected : ProjectEvent()
    object InvitationRemoved : ProjectEvent()

    object OwnerDetailsLoaded : ProjectEvent()

    data class Error(val type: ProjectErrorType, val message: String) : ProjectEvent()
}