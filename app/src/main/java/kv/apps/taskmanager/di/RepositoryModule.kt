package kv.apps.taskmanager.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kv.apps.taskmanager.data.remote.AuthRemoteDataSource
import kv.apps.taskmanager.data.remote.FirestoreListenerManager
import kv.apps.taskmanager.data.remote.GoogleSignInHelper
import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import kv.apps.taskmanager.data.repositoryImpl.AuthRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.NotificationRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.ProjectRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.TaskRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.UserPreferencesRepositoryImpl
import kv.apps.taskmanager.data.repositoryImpl.UserRepositoryImpl
import kv.apps.taskmanager.domain.repository.AuthRepository
import kv.apps.taskmanager.domain.repository.NotificationRepository
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
        authRemoteDataSource: AuthRemoteDataSource,
        googleSignInHelper: GoogleSignInHelper
    ): AuthRepository {
        return AuthRepositoryImpl(firestore, authRemoteDataSource, googleSignInHelper)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        listenerManager: FirestoreListenerManager
    ): UserRepository {
        return UserRepositoryImpl(firestore, firebaseAuth, listenerManager )
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

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        functions: FirebaseFunctions
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore, functions)
    }
}