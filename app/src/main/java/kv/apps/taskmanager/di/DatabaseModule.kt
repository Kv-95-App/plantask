package kv.apps.taskmanager.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kv.apps.taskmanager.data.local.dao.ProjectDao
import kv.apps.taskmanager.data.local.dao.TaskDao
import kv.apps.taskmanager.data.local.database.TaskManagerDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskDao(database: TaskManagerDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideProjectDao(database: TaskManagerDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    @Singleton
    fun provideTaskDatabase(@ApplicationContext context: Context): TaskManagerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TaskManagerDatabase::class.java,
            "task_manager_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}