package kv.apps.taskmanager.data.local.mapper

import kv.apps.taskmanager.data.local.entity.ProjectEntity
import kv.apps.taskmanager.data.local.entity.SyncAction
import kv.apps.taskmanager.domain.model.Project

fun Project.toEntity(
    needsSync: Boolean = false,
    syncAction: SyncAction = SyncAction.NONE,
    isDeleted: Boolean = false
): ProjectEntity {
    return ProjectEntity(
        id = id,
        title = title,
        description = description,
        createdBy = createdBy,
        isCompleted = isCompleted,
        dueDate = dueDate,
        teamMembers = teamMembers,
        isDeleted = isDeleted,
        needsSync = needsSync,
        syncAction = syncAction.name,
        lastModified = System.currentTimeMillis()
    )
}

fun ProjectEntity.toDomain(): Project {
    return Project(
        id = id,
        title = title,
        description = description,
        createdBy = createdBy,
        isCompleted = isCompleted,
        dueDate = dueDate,
        teamMembers = teamMembers
    )
}

fun List<Project>.toEntityList(
    needsSync: Boolean = false,
    syncAction: SyncAction = SyncAction.NONE
): List<ProjectEntity> {
    return map { it.toEntity(needsSync, syncAction) }
}

fun List<ProjectEntity>.toDomainList(): List<Project> {
    return map { it.toDomain() }
}