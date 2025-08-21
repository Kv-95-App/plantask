package kv.apps.taskmanager.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kv.apps.taskmanager.data.remote.AuthRemoteDataSource
import kv.apps.taskmanager.data.remote.FirestoreListenerManager
import kv.apps.taskmanager.data.remote.GoogleSignInHelper
import kv.apps.taskmanager.data.remote.ProjectRemoteDataSource
import kv.apps.taskmanager.data.remote.TaskRemoteDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideProjectRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        listenerManager: FirestoreListenerManager
    ): ProjectRemoteDataSource {
        return ProjectRemoteDataSource(firestore, firebaseAuth, listenerManager)
    }

    @Provides
    @Singleton
    fun provideTaskRemoteDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        listenerManager: FirestoreListenerManager
    ): TaskRemoteDataSource {
        return TaskRemoteDataSource(firestore, firebaseAuth, listenerManager)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(firebaseAuth: FirebaseAuth, googleSignInHelper: GoogleSignInHelper, listenerManager: FirestoreListenerManager): AuthRemoteDataSource {
        return AuthRemoteDataSource(firebaseAuth, googleSignInHelper, listenerManager)
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFunctions(): FirebaseFunctions = FirebaseFunctions.getInstance()

    @Provides
    @Singleton
    fun provideGoogleSignInHelper(
        @ApplicationContext context: Context,
        firebaseAuth: FirebaseAuth,
        listenerManager: FirestoreListenerManager
    ): GoogleSignInHelper {
        return GoogleSignInHelper(context, firebaseAuth, listenerManager)
    }

}