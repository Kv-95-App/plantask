package kv.apps.taskmanager.domain.model

import java.util.Date

data class TeamMember(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val joinedAt: Date? = null,
    val email: String? = null,
)