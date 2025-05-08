package kv.apps.taskmanager.domain.model

data class Project (
    val id: String = "",
    val title: String = "",
    val description: String= "",
    val createdBy: String = "",
    val isCompleted: Boolean = false,
    val dueDate: String = "",
    val teamMembers: List<String> = emptyList(),
){
    constructor() : this("", "", "", "", false, "", emptyList())
}