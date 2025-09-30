package kv.apps.taskmanager.data.local.mapper

import kv.apps.taskmanager.data.local.entity.SyncAction
import kv.apps.taskmanager.data.local.entity.TaskEntity
import kv.apps.taskmanager.domain.model.Task

fun Task.toEntity(
    needsSync: Boolean = false,
    syncAction: SyncAction = SyncAction.NONE,
    isDeleted: Boolean = false
): TaskEntity {
    return TaskEntity(
        id = id,
        assignedTo = assignedTo,
        isCompleted = isCompleted,
        title = title,
        taskDetails = taskDetails,
        dueDate = dueDate,
        projectId = projectId,
        commentCount = commentCount,
        isDeleted = isDeleted,
        needsSync = needsSync,
        syncAction = syncAction.name,
        lastModified = System.currentTimeMillis()
    )
}

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        assignedTo = assignedTo,
        isCompleted = isCompleted,
        title = title,
        taskDetails = taskDetails,
        dueDate = dueDate,
        projectId = projectId,
        commentCount = commentCount
    )
}

fun List<Task>.toEntityList(
    needsSync: Boolean = false,
    syncAction: SyncAction = SyncAction.NONE
): List<TaskEntity> {
    return map { it.toEntity(needsSync, syncAction) }
}

fun List<TaskEntity>.toDomainList(): List<Task> {
    return map { it.toDomain() }
}