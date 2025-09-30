package kv.apps.taskmanager.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import kv.apps.taskmanager.data.local.converters.ListStringConverter
import kv.apps.taskmanager.data.local.dao.ProjectDao
import kv.apps.taskmanager.data.local.dao.TaskDao
import kv.apps.taskmanager.data.local.entity.ProjectEntity
import kv.apps.taskmanager.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        ProjectEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListStringConverter::class)
abstract class TaskManagerDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: TaskManagerDatabase? = null

        fun getDatabase(context: Context): TaskManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskManagerDatabase::class.java,
                    "task_manager_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}