package kv.apps.taskmanager.data.remote

import android.util.Log
import com.google.firebase.firestore.ListenerRegistration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreListenerManager @Inject constructor() {
    private val snapshotListeners = mutableListOf<ListenerRegistration>()
    private val activeQueries = mutableListOf<String>()

    fun addSnapshotListener(registration: ListenerRegistration, description: String = "unknown") {
        snapshotListeners.add(registration)
        Log.d("FirestoreListenerManager", "Snapshot listener added for: $description. Total: ${snapshotListeners.size}")
    }

    fun addActiveQuery(queryDescription: String) {
        activeQueries.add(queryDescription)
        Log.d("FirestoreListenerManager", "Active query added: $queryDescription. Total: ${activeQueries.size}")
    }

    fun removeAllListeners() {
        val snapshotCount = snapshotListeners.size
        snapshotListeners.forEach { it.remove() }
        snapshotListeners.clear()

        val queryCount = activeQueries.size
        activeQueries.clear()

        Log.d("FirestoreListenerManager", "Removed $snapshotCount snapshot listeners and $queryCount active queries")
    }

}