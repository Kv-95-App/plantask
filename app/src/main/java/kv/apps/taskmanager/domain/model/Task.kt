package kv.apps.taskmanager.domain.model

data class Task(
    val id: String = "", // Firestore document ID
    val assignedTo: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val title: String = "",
    val taskDetails: String = "",
    val dueDate: String = ""
)
