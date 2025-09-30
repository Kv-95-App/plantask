package kv.apps.taskmanager.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.data.local.entity.TaskEntity

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND isDeleted = 0")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND isDeleted = 0")
    suspend fun getTasksForProjectSync(projectId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :taskId AND isDeleted = 0")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND dueDate = :date AND isDeleted = 0")
    suspend fun getTasksByDueDate(projectId: String, date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND isDeleted = 0 ORDER BY dueDate ASC")
    suspend fun getTasksSortedByDueDateAsc(projectId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND isDeleted = 0 ORDER BY dueDate DESC")
    suspend fun getTasksSortedByDueDateDesc(projectId: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE needsSync = 1")
    suspend fun getTasksNeedingSync(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET isDeleted = 1, needsSync = 1, syncAction = 'DELETE', lastModified = :timestamp WHERE id = :taskId")
    suspend fun markTaskAsDeleted(taskId: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM tasks WHERE projectId = :projectId")
    suspend fun deleteTasksForProject(projectId: String)

    @Query("UPDATE tasks SET needsSync = 0 WHERE id = :taskId")
    suspend fun markTaskAsSynced(taskId: String)

    @Query("DELETE FROM tasks WHERE isDeleted = 1")
    suspend fun cleanupDeletedTasks()
}