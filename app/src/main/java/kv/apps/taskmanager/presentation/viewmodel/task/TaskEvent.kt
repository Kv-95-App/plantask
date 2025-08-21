package kv.apps.taskmanager.presentation.viewmodel.task

sealed class TaskEvent {
    object TasksLoaded : TaskEvent()
    object TaskAdded : TaskEvent()
    object TaskUpdated : TaskEvent()
    object TaskDeleted : TaskEvent()
    object TaskFetched : TaskEvent()
    data class Error(val type: TaskErrorType, val message: String) : TaskEvent()
    object TaskMembersLoaded : TaskEvent()
    data class CommentAdded(val commentId: String) : TaskEvent()
    data class CommentUpdated(val commentId: String) : TaskEvent()
    data class CommentDeleted(val commentId: String) : TaskEvent()
    object CommentsLoaded : TaskEvent()
}