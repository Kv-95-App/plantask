package kv.apps.taskmanager.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val birthday: String? = null,
    val fcmToken: String? = null,

    @ServerTimestamp
    val fcmTokenUpdated: Date? = null,

    @ServerTimestamp
    val updatedAt: Date? = null,

    @ServerTimestamp
    val createdAt: Date? = null
) {
    constructor() : this("", "", "", "", "", null, null, null, null, null)

}