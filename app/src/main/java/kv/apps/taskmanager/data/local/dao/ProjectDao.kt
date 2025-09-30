package kv.apps.taskmanager.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.data.local.entity.ProjectEntity

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects WHERE isDeleted = 0")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE isDeleted = 0")
    suspend fun getAllProjectsSync(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :projectId AND isDeleted = 0")
    suspend fun getProjectById(projectId: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :projectId AND isDeleted = 0")
    fun getProjectByIdFlow(projectId: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE needsSync = 1")
    suspend fun getProjectsNeedingSync(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET isDeleted = 1, needsSync = 1, syncAction = 'DELETE', lastModified = :timestamp WHERE id = :projectId")
    suspend fun markProjectAsDeleted(
        projectId: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: String)

    @Query("UPDATE projects SET needsSync = 0 WHERE id = :projectId")
    suspend fun markProjectAsSynced(projectId: String)

    @Query("DELETE FROM projects WHERE isDeleted = 1")
    suspend fun cleanupDeletedProjects()
}