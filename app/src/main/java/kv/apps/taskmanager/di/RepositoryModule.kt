package kv.apps.taskmanager.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kv.apps.taskmanager.data.remote.AuthRemoteDataSource
import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import kv.apps.taskmanager.data.repositoryImpl.AuthRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.ProjectRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.TaskRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.UserPreferencesRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.UserRepositoryImpl
import kv.apps.taskmanager.domain.repository.AuthRepository
import kv.apps.taskmanager.domain.repository.ProjectRepository
import kv.apps.taskmanager.domain.repository.TaskRepository
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        firestore: FirebaseFirestore,
        authRemoteDataSource: AuthRemoteDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(firestore, authRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): UserRepository {
        return UserRepositoryImpl(firestore, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectRemoteDataSource: ProjectRemoteDataSource
    ): ProjectRepository {
        return ProjectRepositoryImpl(projectRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskRemoteDataSource: TaskRemoteDataSource
    ): TaskRepository {
        return TaskRepositoryImpl(taskRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        dataStore: DataStore<Preferences>
    ): UserPreferencesRepository {
        return UserPreferencesRepositoryImpl(dataStore)
    }
}