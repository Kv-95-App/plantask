package kv.apps.taskmanager.domain.model

data class Task(
    val id: String = "",
    val assignedTo: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val title: String = "",
    val taskDetails: String = "",
    val dueDate: String = "",
    val projectId: String = ""
)
