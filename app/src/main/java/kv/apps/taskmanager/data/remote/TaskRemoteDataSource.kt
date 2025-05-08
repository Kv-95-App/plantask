package kv.apps.taskmanager.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Task
import java.time.LocalDate
import javax.inject.Inject



class TaskRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getTasksForProject(projectId: String): List<Task> = withContext(Dispatchers.IO) {
        val tasks = mutableListOf<Task>()

        try {
            val snapshot = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .get()
                .await()

            for (doc in snapshot.documents) {
                val data = doc.data ?: continue

                val assignedToList = (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                tasks.add(
                    Task(
                        id = doc.id,
                        assignedTo = assignedToList,
                        isCompleted = data["isCompleted"] as? Boolean == true,
                        title = data["title"] as? String ?: "",
                        taskDetails = data["taskDetails"] as? String ?: "",
                        dueDate = data["dueDate"] as? String ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch tasks", e)
        }

        tasks
    }


    suspend fun addTaskToProject(projectId: String, task: Task): Unit = withContext(Dispatchers.IO) {
        try {
            val data: Map<String, Any?> = mapOf(
                "title" to task.title,
                "taskDetails" to task.taskDetails,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted,
                "assignedTo" to task.assignedTo
            )

            firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .add(data)
                .await()
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to add task", e)
        }
    }


    suspend fun updateTaskInProject(projectId: String, task: Task): Unit = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "title" to task.title,
                "taskDetails" to task.taskDetails,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted,
                "assignedTo" to task.assignedTo
            )

            firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(task.id)
                .set(data)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to update task: ${e.message}")
        }
    }

    suspend fun deleteTaskFromProject(projectId: String, taskId: String): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to delete task: ${e.message}")
        }
    }

    suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            val data = snapshot.data ?: return@withContext null

            val assignedToList = (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            Task(
                id = snapshot.id,
                assignedTo = assignedToList,
                isCompleted = data["isCompleted"] as? Boolean == true,
                title = data["title"] as? String ?: "",
                taskDetails = data["taskDetails"] as? String ?: "",
                dueDate = data["dueDate"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch task", e)
            null
        }
    }


    suspend fun getTasksSortedByDueDate(projectId: String, ascending: Boolean): List<Task> = withContext(Dispatchers.IO) {
        try {
            val query = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .orderBy("dueDate", if (ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)

            val snapshot = query.get().await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val assignedToList = (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                Task(
                    id = doc.id,
                    assignedTo = assignedToList,
                    isCompleted = data["isCompleted"] == true,
                    title = data["title"] as? String ?: "",
                    taskDetails = data["taskDetails"] as? String ?: "",
                    dueDate = data["dueDate"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to fetch sorted tasks", e)
            emptyList()
        }
    }


    suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task> = withContext(Dispatchers.IO) {
        try {
            val formattedDate = date.toString()

            val snapshot = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .whereEqualTo("dueDate", formattedDate)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val assignedToList = (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                Task(
                    id = doc.id,
                    assignedTo = assignedToList,
                    isCompleted = data["isCompleted"] == true,
                    title = data["title"] as? String ?: "",
                    taskDetails = data["taskDetails"] as? String ?: "",
                    dueDate = data["dueDate"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to filter tasks by date", e)
            emptyList()
        }
    }

}