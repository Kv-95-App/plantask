package kv.apps.taskmanager.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthday: String? = null
)