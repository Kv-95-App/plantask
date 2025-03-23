package kv.apps.taskmanager.data.remote

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
                doc.data?.let {
                    tasks.add(
                        Task(
                            id = doc.id,
                            assignedTo = it["assignedTo"] as? List<String> ?: emptyList(),
                            isCompleted = it["isCompleted"] as? Boolean ?: false,
                            title = it["title"] as? String ?: "",
                            taskDetails = it["taskDetails"] as? String ?: "",
                            dueDate = it["dueDate"] as? String ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch tasks: ${e.message}")
        }

        return@withContext tasks
    }

    suspend fun addTaskToProject(projectId: String, task: Task) = withContext(Dispatchers.IO) {
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
                .add(data)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to add task: ${e.message}")
        }
    }

    suspend fun updateTaskInProject(projectId: String, task: Task) = withContext(Dispatchers.IO) {
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

    suspend fun deleteTaskFromProject(projectId: String, taskId: String) = withContext(Dispatchers.IO) {
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

            return@withContext snapshot.data?.let {
                Task(
                    id = snapshot.id,
                    assignedTo = it["assignedTo"] as? List<String> ?: emptyList(),
                    isCompleted = it["isCompleted"] as? Boolean ?: false,
                    title = it["title"] as? String ?: "",
                    taskDetails = it["taskDetails"] as? String ?: "",
                    dueDate = it["dueDate"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch task: ${e.message}")
        }
    }

    suspend fun getTasksSortedByDueDate(projectId: String, ascending: Boolean): List<Task> = withContext(Dispatchers.IO) {
        val tasks = mutableListOf<Task>()

        try {
            val query = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .orderBy("dueDate", if (ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING)

            val snapshot = query.get().await()

            for (doc in snapshot.documents) {
                doc.data?.let {
                    tasks.add(
                        Task(
                            id = doc.id,
                            assignedTo = it["assignedTo"] as? List<String> ?: emptyList(),
                            isCompleted = it["isCompleted"] as? Boolean ?: false,
                            title = it["title"] as? String ?: "",
                            taskDetails = it["taskDetails"] as? String ?: "",
                            dueDate = it["dueDate"] as? String ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch sorted tasks: ${e.message}")
        }

        return@withContext tasks
    }

    suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task> = withContext(Dispatchers.IO) {
        val tasks = mutableListOf<Task>()
        val formattedDate = date.toString()

        try {
            val snapshot = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .whereEqualTo("dueDate", formattedDate)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.data?.let {
                    tasks.add(
                        Task(
                            id = doc.id,
                            assignedTo = it["assignedTo"] as? List<String> ?: emptyList(),
                            isCompleted = it["isCompleted"] as? Boolean ?: false,
                            title = it["title"] as? String ?: "",
                            taskDetails = it["taskDetails"] as? String ?: "",
                            dueDate = it["dueDate"] as? String ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to filter tasks by date: ${e.message}")
        }

        return@withContext tasks
    }
}