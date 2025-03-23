package kv.apps.taskmanager.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kv.apps.taskmanager.domain.repository.AuthRepository
import kv.apps.taskmanager.domain.repository.ProjectRepository
import kv.apps.taskmanager.domain.repository.TaskRepository
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import kv.apps.taskmanager.domain.repository.UserRepository
import kv.apps.taskmanager.domain.usecase.userUseCases.FetchUserDetailsUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.LoginUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.RegisterUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.ResetPasswordUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.SessionUseCase
import kv.apps.taskmanager.domain.usecase.projectsUseCases.ProjectUseCases
import kv.apps.taskmanager.domain.usecase.tasksUseCases.TaskUseCases
import kv.apps.taskmanager.domain.usecase.userUseCases.AcceptFriendRequestUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.AddFriendUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.DeleteFriendUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetFriendsUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetPendingFriendRequestsUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.RejectFriendRequestUseCase
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideTaskUseCases(
        taskRepository: TaskRepository
    ): TaskUseCases {
        return TaskUseCases(taskRepository)
    }

    @Provides
    @Singleton
    fun provideProjectUseCase(
        projectRepository: ProjectRepository
    ): ProjectUseCases {
        return ProjectUseCases(projectRepository)
    }

    @Provides
    @Singleton
    fun provideSessionUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): SessionUseCase {
        return SessionUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideAcceptFriendRequestUseCase(
        userRepository: UserRepository
    ): AcceptFriendRequestUseCase {
        return AcceptFriendRequestUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideAddFriendUseCase(
        userRepository: UserRepository
    ): AddFriendUseCase {
        return AddFriendUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideDeleteFriendUseCase(
        userRepository: UserRepository
    ): DeleteFriendUseCase {
        return DeleteFriendUseCase(userRepository)
    }
    @Provides
    @Singleton
    fun provideGetFriendsUseCase(
        userRepository: UserRepository
    ): GetFriendsUseCase {
        return GetFriendsUseCase(userRepository)
    }
    @Provides
    @Singleton
    fun provideGetPendingFriendRequestsUseCase(
        userRepository: UserRepository
    ): GetPendingFriendRequestsUseCase {
        return GetPendingFriendRequestsUseCase(userRepository)
    }
    @Provides
    @Singleton
    fun provideRejectFriendRequestUseCase(
        userRepository: UserRepository
    ): RejectFriendRequestUseCase {
        return RejectFriendRequestUseCase(userRepository)
    }
    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase {
        return LoginUseCase(authRepository)
    }
    @Provides
    @Singleton
    fun provideRegisterUseCase(
        authRepository: AuthRepository
    ): RegisterUseCase {
        return RegisterUseCase(authRepository)
    }
    @Provides
    @Singleton
    fun provideResetPasswordUseCase(
        authRepository: AuthRepository
    ): ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepository)
    }
    @Provides
    @Singleton
    fun provideFetchUserDetailsUseCase(
        userRepository: UserRepository
    ): FetchUserDetailsUseCase {
        return FetchUserDetailsUseCase(userRepository)
    }


}