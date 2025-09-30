package kv.apps.taskmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kv.apps.taskmanager.data.local.converters.ListStringConverter

@Entity(tableName = "tasks")
@TypeConverters(ListStringConverter::class)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val assignedTo: List<String>,
    val isCompleted: Boolean,
    val title: String,
    val taskDetails: String,
    val dueDate: String,
    val projectId: String,
    val commentCount: Long,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val syncAction: String = SyncAction.NONE.name
)

enum class SyncAction {
    CREATE, UPDATE, DELETE, NONE
}