package kv.apps.taskmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kv.apps.taskmanager.data.local.converters.ListStringConverter

@Entity(tableName = "projects")
@TypeConverters(ListStringConverter::class)
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val createdBy: String,
    val isCompleted: Boolean,
    val dueDate: String,
    val teamMembers: List<String>,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val syncAction: String = SyncAction.NONE.name
)