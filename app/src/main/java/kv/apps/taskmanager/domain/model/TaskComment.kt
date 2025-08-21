package kv.apps.taskmanager.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class TaskComment(
    val id: String = "",
    val taskId: String = "",
    val projectId: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val message: String = "",
    val timestamp: Date = Date(),
    val isEdited: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", Date(), false)
}