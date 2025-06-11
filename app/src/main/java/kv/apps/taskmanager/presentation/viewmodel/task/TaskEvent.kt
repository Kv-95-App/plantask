package kv.apps.taskmanager.presentation.viewmodel.task

sealed class TaskEvent {
    object TasksLoaded : TaskEvent()
    object TaskAdded : TaskEvent()
    object TaskUpdated : TaskEvent()
    object TaskDeleted : TaskEvent()
    object TaskFetched : TaskEvent()
    data class Error(val type: TaskErrorType, val message: String) : TaskEvent()
}