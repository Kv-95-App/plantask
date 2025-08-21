package kv.apps.taskmanager.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.domain.model.User
import java.time.LocalDate
import javax.inject.Inject

class TaskRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val listenerManager: FirestoreListenerManager
) {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getTasksForProject(projectId: String): List<Task> = withContext(Dispatchers.IO) {
        val tasks = mutableListOf<Task>()

        val snapshot = firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .get()
            .await()

        for (doc in snapshot.documents) {
            val data = doc.data ?: continue

            val assignedToList =
                (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            tasks.add(
                Task(
                    id = doc.id,
                    assignedTo = assignedToList,
                    isCompleted = data["isCompleted"] as? Boolean == true,
                    title = data["title"] as? String ?: "",
                    taskDetails = data["taskDetails"] as? String ?: "",
                    dueDate = data["dueDate"] as? String ?: "",
                    projectId = projectId
                )
            )
        }

        tasks
    }

    suspend fun addTaskToProject(projectId: String, task: Task): Unit =
        withContext(Dispatchers.IO) {
            val data: Map<String, Any?> = mapOf(
                "title" to task.title,
                "taskDetails" to task.taskDetails,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted,
                "assignedTo" to task.assignedTo,
                "projectId" to task.projectId
            )

            firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .add(data)
                .await()
        }

    suspend fun getMembersOfTasks(projectId: String, taskId: String): List<User> =
        withContext(Dispatchers.IO) {
            val taskDoc = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            if (!taskDoc.exists()) {
                return@withContext emptyList()
            }

            val assignedToList = taskDoc.get("assignedTo") as? List<String> ?: emptyList()

            if (assignedToList.isEmpty()) {
                return@withContext emptyList()
            }

            assignedToList.mapNotNull { userId ->
                firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                    .toObject(User::class.java)
            }
        }

    suspend fun getTaskAssignedUsersInitials(
        projectId: String,
        taskId: String
    ): Map<String, String> =
        withContext(Dispatchers.IO) {
            val taskDoc = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            val assignedUserIds = (taskDoc["assignedTo"] as? List<String>) ?: emptyList()
            if (assignedUserIds.isEmpty()) return@withContext emptyMap()

            val usersSnapshot = firestore.collection("users")
                .whereIn(FieldPath.documentId(), assignedUserIds)
                .get()
                .await()

            usersSnapshot.documents.associate { doc ->
                val firstName = doc.getString("firstName") ?: ""
                val lastName = doc.getString("lastName") ?: ""
                val initials = "${firstName.firstOrNull()?.uppercase()}${
                    lastName.firstOrNull()?.uppercase()
                }"
                doc.id to initials
            }
        }

    suspend fun updateTaskInProject(projectId: String, task: Task): Unit =
        withContext(Dispatchers.IO) {
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
        }

    suspend fun deleteTaskFromProject(projectId: String, taskId: String): Unit =
        withContext(Dispatchers.IO) {
            firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()
        }

    suspend fun getTaskByIdFromProject(projectId: String, taskId: String): Task? =
        withContext(Dispatchers.IO) {
            val taskSnapshot = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            val commentsQuery = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .document(taskId)
                .collection("comments")

            val countQuery = commentsQuery.count()
            val aggregateQuerySnapshot = countQuery.get(AggregateSource.SERVER).await()
            val commentsCount = aggregateQuerySnapshot.count

            val data = taskSnapshot.data ?: return@withContext null

            Task(
                id = taskSnapshot.id,
                assignedTo = (data["assignedTo"] as? List<*>)?.filterIsInstance<String>()
                    ?: emptyList(),
                isCompleted = data["isCompleted"] as? Boolean == true,
                title = data["title"] as? String ?: "",
                taskDetails = data["taskDetails"] as? String ?: "",
                dueDate = data["dueDate"] as? String ?: "",
                projectId = projectId,
                commentCount = commentsCount
            )
        }

    suspend fun getTasksSortedByDueDate(projectId: String, ascending: Boolean): List<Task> =
        withContext(Dispatchers.IO) {
            val query = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .orderBy(
                    "dueDate",
                    if (ascending) Query.Direction.ASCENDING else Query.Direction.DESCENDING
                )

            val snapshot = query.get().await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val assignedToList =
                    (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                Task(
                    id = doc.id,
                    assignedTo = assignedToList,
                    isCompleted = data["isCompleted"] == true,
                    title = data["title"] as? String ?: "",
                    taskDetails = data["taskDetails"] as? String ?: "",
                    dueDate = data["dueDate"] as? String ?: "",
                    projectId = projectId
                )
            }
        }

    suspend fun filterTasksByDueDate(projectId: String, date: LocalDate): List<Task> =
        withContext(Dispatchers.IO) {
            val formattedDate = date.toString()

            val snapshot = firestore.collection("projects")
                .document(projectId)
                .collection("tasks")
                .whereEqualTo("dueDate", formattedDate)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val assignedToList =
                    (data["assignedTo"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

                Task(
                    id = doc.id,
                    assignedTo = assignedToList,
                    isCompleted = data["isCompleted"] == true,
                    title = data["title"] as? String ?: "",
                    taskDetails = data["taskDetails"] as? String ?: "",
                    dueDate = data["dueDate"] as? String ?: "",
                    projectId = projectId
                )
            }
        }

    fun getProjectUsers(projectId: String): Flow<List<User>> = flow {
        val projectDoc = firestore.collection("projects")
            .document(projectId)
            .get()
            .await()

        val teamMemberIds = projectDoc.get("teamMembers") as? List<String> ?: emptyList()

        val users = teamMemberIds.mapNotNull { userId ->
            firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(User::class.java)
        }

        emit(users)
    }

    suspend fun addCommentToTask(
        projectId: String,
        taskId: String,
        message: String
    ): Result<String> = try {
        val userId = currentUserId ?: throw Exception("User not logged in")

        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        val firstName = userDoc.getString("firstName") ?: ""
        val lastName = userDoc.getString("lastName") ?: ""
        val displayName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
            "$firstName $lastName".trim()
        } else {
            userDoc.getString("email")?.split("@")?.first() ?: userId.take(8)
        }

        val commentId = firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .document().id

        val comment = TaskComment(
            id = commentId,
            taskId = taskId,
            projectId = projectId,
            userId = userId,
            userDisplayName = displayName,
            message = message
        )

        firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .document(commentId)
            .set(comment)
            .await()

        Result.success(commentId)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to add comment: ${e.message}"))
    }

    suspend fun getTaskComments(
        projectId: String,
        taskId: String,
        limit: Int = 50
    ): List<TaskComment> = try {
        firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects(TaskComment::class.java)
    } catch (e: Exception) {
        throw Exception("Failed to fetch comments: ${e.message}")
    }

    suspend fun editTaskComment(
        projectId: String,
        taskId: String,
        commentId: String,
        newMessage: String
    ): Result<Unit> = try {
        val userId = currentUserId ?: throw Exception("User not logged in")

        val commentDoc = firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .document(commentId)
            .get()
            .await()

        if (commentDoc.getString("userId") != userId) {
            throw Exception("Only the comment author can edit the comment")
        }

        firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .document(commentId)
            .update(
                mapOf(
                    "message" to newMessage,
                    "isEdited" to true
                )
            )
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to edit comment: ${e.message}"))
    }

    suspend fun deleteTaskComment(
        projectId: String,
        taskId: String,
        commentId: String
    ): Result<Unit> = try {
        val userId = currentUserId ?: throw Exception("User not logged in")

        val commentDoc = firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .document(commentId)
            .get()
            .await()

        val isCommentOwner = commentDoc.getString("userId") == userId
        val isTaskAdmin = isUserTaskAdmin(projectId, userId)

        if (!isCommentOwner && !isTaskAdmin) {
            throw Exception("You don't have permission to delete this comment")
        }

        firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .document(commentId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to delete comment: ${e.message}"))
    }

    private suspend fun isUserTaskAdmin(
        projectId: String,
        userId: String
    ): Boolean {
        val projectDoc = firestore.collection("projects")
            .document(projectId)
            .get()
            .await()

        val createdBy = projectDoc.getString("createdBy") ?: ""
        if (createdBy == userId) return true

        val memberDoc = firestore.collection("projects")
            .document(projectId)
            .collection("teamMembers")
            .document(userId)
            .get()
            .await()

        return memberDoc.getBoolean("isAdmin") ?: false
    }

    fun observeTaskComments(
        projectId: String,
        taskId: String,
        limit: Int = 50
    ): Flow<List<TaskComment>> = callbackFlow {
        val listenerRegistration = firestore.collection("projects")
            .document(projectId)
            .collection("tasks")
            .document(taskId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
                    ) {
                        return@addSnapshotListener
                    }
                    close(error)
                    return@addSnapshotListener
                }

                val comments = snapshot?.toObjects(TaskComment::class.java) ?: emptyList()
                trySend(comments).isSuccess
            }

        listenerManager.addSnapshotListener(
            listenerRegistration,
            "task_comments:project=$projectId,task=$taskId"
        )

        awaitClose {
            listenerRegistration.remove()
        }
    }
}