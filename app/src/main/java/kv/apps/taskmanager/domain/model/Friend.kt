package kv.apps.taskmanager.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.Date

@Keep
@IgnoreExtraProperties
data class Friend(
    @PropertyName("friendId") val friendId: String = "",
    @PropertyName("displayName") val displayName: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("addedAt") val addedAt: Date? = null
) {
    constructor() : this("", "", "", null)
}