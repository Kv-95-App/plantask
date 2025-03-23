package kv.apps.taskmanager.domain.model

import com.google.firebase.Timestamp

data class ProjectInvitation(
    val invitationId: String = "", // Unique ID for the invitation
    val projectId: String = "",    // ID of the project the user is being invited to
    val fromUserId: String = "",   // ID of the user sending the invitation
    val toUserId: String = "",     // ID of the user being invited
    val status: String = "pending", // Status of the invitation: "pending", "accepted", "rejected"
    val timestamp: Timestamp = Timestamp.now() // When the invitation was sent
)