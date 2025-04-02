package kv.apps.taskmanager.domain.model

import com.google.firebase.Timestamp

data class ProjectInvitation(
    val invitationId: String = "",
    val projectId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending",
    val timestamp: Timestamp = Timestamp.now()
)