package kv.apps.taskmanager.data.remote

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.TeamMember
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ProjectRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val listenerManager: FirestoreListenerManager
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private suspend fun fetchTeamMembers(projectId: String): List<String> {
        return try {
            firestore.collection("projects")
                .document(projectId)
                .collection("teamMembers")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.getString("userId") ?: document.id.takeIf { it.isNotBlank() }
                }
                .distinct()
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            when (e) {
                is CancellationException -> throw e
                else -> emptyList()
            }
        }
    }

    private suspend fun getUserDetails(userId: String): Pair<String, String> =
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            Pair(
                snapshot.getString("firstName") ?: "",
                snapshot.getString("lastName") ?: ""
            )
        } catch (_: Exception) {
            Pair("", "")
        }

    suspend fun getAllProjectsForUser(): List<Project> {
        val currentUserId = currentUserId ?: throw Exception("User not logged in")
        val projects = mutableListOf<Project>()

        try {
            listenerManager.addActiveQuery("collectionGroup:teamMembers for user: $currentUserId")

            val createdProjects = firestore.collection("projects")
                .whereEqualTo("createdBy", currentUserId)
                .get()
                .await()

            val memberProjects = firestore.collectionGroup("teamMembers")
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.reference.parent.parent?.id }

            val allProjectIds =
                (createdProjects.documents.map { it.id } + memberProjects).distinct()

            val projectSnapshots = firestore.collection("projects")
                .whereIn(FieldPath.documentId(), allProjectIds)
                .get()
                .await()

            projectSnapshots.documents.forEach { doc ->
                doc.data?.let { data ->
                    projects.add(
                        Project(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            createdBy = data["createdBy"] as? String ?: "",
                            isCompleted = data["isCompleted"] as? Boolean == true,
                            dueDate = data["dueDate"] as? String ?: "",
                            teamMembers = fetchTeamMembers(doc.id)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch projects: ${e.message}")
        }

        return projects
    }

    suspend fun addTeamMembersToProject(
        projectId: String,
        teamMemberIds: List<String>
    ): Result<Unit> = try {
        val batch = firestore.batch()

        for (userId in teamMemberIds) {
            try {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""
                val name = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                    "$firstName $lastName".trim()
                } else {
                    userDoc.getString("email") ?: userId.take(8)
                }

                val email = userDoc.getString("email") ?: ""

                val memberRef = firestore.collection("projects")
                    .document(projectId)
                    .collection("teamMembers")
                    .document(userId)

                batch.set(
                    memberRef, mapOf(
                        "userId" to userId,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "displayName" to name,
                        "email" to email,
                        "joinedAt" to FieldValue.serverTimestamp()
                    )
                )
            } catch (_: Exception) {
                val fallbackName = "User ${userId.take(8)}"
                val memberRef = firestore.collection("projects")
                    .document(projectId)
                    .collection("teamMembers")
                    .document(userId)

                batch.set(
                    memberRef, mapOf(
                        "userId" to userId,
                        "firstName" to "",
                        "lastName" to "",
                        "displayName" to fallbackName,
                        "email" to "",
                        "joinedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
        }

        batch.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to add team members: ${e.message}"))
    }

    suspend fun getProjectById(projectId: String): Project? =
        try {
            val doc = firestore.collection("projects")
                .document(projectId)
                .get()
                .await()

            doc.data?.let { data ->
                val createdBy = data["createdBy"] as? String ?: ""
                val (ownerFirstName, ownerLastName) = getUserDetails(createdBy)

                Project(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    createdBy = createdBy,
                    isCompleted = data["isCompleted"] as? Boolean == true,
                    dueDate = data["dueDate"] as? String ?: "",
                    teamMembers = fetchTeamMembers(doc.id)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch project: ${e.message}")
        }

    suspend fun removeTeamMembersFromProject(
        projectId: String,
        teamMemberId: String
    ): Result<Unit> =
        try {
            firestore.collection("projects")
                .document(projectId)
                .collection("teamMembers")
                .document(teamMemberId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to remove team member: ${e.message}"))
        }

    suspend fun getTeamMembersForProject(projectId: String): List<TeamMember> {
        return try {
            val snapshot = firestore.collection("projects")
                .document(projectId)
                .collection("teamMembers")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                TeamMember(
                    userId = doc.id,
                    firstName = doc.getString("firstName") ?: "",
                    lastName = doc.getString("lastName") ?: "",
                    joinedAt = doc.getDate("joinedAt"),
                    email = doc.getString("email")
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch team members: ${e.message}")
        }
    }

    suspend fun createProject(project: Project): String =
        try {
            val currentUserId = currentUserId ?: throw Exception("User not logged in")

            val userDoc = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val firstName = userDoc.getString("firstName") ?: ""
            val lastName = userDoc.getString("lastName") ?: ""
            val email = userDoc.getString("email") ?: ""
            val displayName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                "$firstName $lastName".trim()
            } else {
                email.ifEmpty { currentUserId.take(8) }
            }

            val data = mapOf(
                "title" to project.title,
                "description" to project.description,
                "createdBy" to currentUserId,
                "createdByName" to displayName,
                "isCompleted" to project.isCompleted,
                "dueDate" to project.dueDate,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val projectId = project.id.ifEmpty {
                firestore.collection("projects").document().id
            }

            firestore.collection("projects")
                .document(projectId)
                .set(data)
                .await()

            firestore.collection("projects")
                .document(projectId)
                .collection("teamMembers")
                .document(currentUserId)
                .set(
                    mapOf(
                        "userId" to currentUserId,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "displayName" to displayName,
                        "email" to email,
                        "joinedAt" to FieldValue.serverTimestamp(),
                        "isAdmin" to true
                    )
                )
                .await()

            projectId
        } catch (e: Exception) {
            throw Exception("Failed to create project: ${e.message}")
        }


            suspend fun deleteProject(projectId: String) {
                try {
                    deleteSubcollectionWithoutRead("projects/$projectId/teamMembers")
                    deleteSubcollectionWithoutRead("projects/$projectId/projectInvitations")
                    deleteSubcollectionWithoutRead("projects/$projectId/tasks")

                    firestore.collection("projects")
                        .document(projectId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    throw Exception("Failed to delete project: ${e.message}")
                }
            }

            private suspend fun deleteSubcollectionWithoutRead(path: String) {
                try {
                    val documents = firestore.collection(path)
                        .limit(500)
                        .get()
                        .await()

                    if (documents.isEmpty) return

                    val batch = firestore.batch()
                    documents.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                    deleteSubcollectionWithoutRead(path)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }
            }

            suspend fun getProjectCreatorDetails(createdById: String): Result<Pair<String, String>> =
                try {
                    val snapshot = firestore.collection("users")
                        .document(createdById)
                        .get()
                        .await()

                    val firstName = snapshot.getString("firstName") ?: ""
                    val lastName = snapshot.getString("lastName") ?: ""

                    Result.success(Pair(firstName, lastName))
                } catch (e: Exception) {
                    Result.failure(Exception("Failed to fetch creator details: ${e.message}"))
                }

            suspend fun updateProject(projectId: String, project: Project): Unit =
                try {
                    val currentMembers = fetchTeamMembers(projectId)
                    val newMembers = project.teamMembers

                    val data = mapOf(
                        "title" to project.title,
                        "description" to project.description,
                        "isCompleted" to project.isCompleted,
                        "dueDate" to project.dueDate,
                        "teamMembers" to project.teamMembers
                    )

                    firestore.collection("projects")
                        .document(projectId)
                        .update(data)
                        .await()

                    val membersToAdd = newMembers - currentMembers
                    if (membersToAdd.isNotEmpty()) {
                        addTeamMembersToProject(projectId, membersToAdd)
                    }

                    val membersToRemove = currentMembers - newMembers
                    membersToRemove.forEach { memberId ->
                        removeTeamMembersFromProject(projectId, memberId)
                    }
                } catch (e: Exception) {
                    throw Exception("Failed to update project: ${e.message}")
                }

            suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit> {
                return try {
                    val isAlreadyMember = firestore.collection("projects")
                        .document(invitation.projectId)
                        .collection("teamMembers")
                        .document(invitation.toUserId)
                        .get()
                        .await()
                        .exists()

                    if (isAlreadyMember) {
                        return Result.failure(Exception("User is already a team member"))
                    }

                    val existingInvitation = firestore.collection("projects")
                        .document(invitation.projectId)
                        .collection("projectInvitations")
                        .whereEqualTo("toUserId", invitation.toUserId)
                        .whereEqualTo("status", "pending")
                        .get()
                        .await()

                    if (!existingInvitation.isEmpty) {
                        return Result.failure(Exception("User already has a pending invitation"))
                    }

                    val invitationId = invitation.invitationId.ifEmpty {
                        firestore.collection("projects")
                            .document(invitation.projectId)
                            .collection("projectInvitations")
                            .document().id
                    }

                    val invitationToSave = invitation.copy(
                        invitationId = invitationId,
                        status = "pending",
                        timestamp = Timestamp.now()
                    )

                    firestore.collection("projects")
                        .document(invitation.projectId)
                        .collection("projectInvitations")
                        .document(invitationId)
                        .set(invitationToSave)
                        .await()

                    sendProjectInvitationNotification(invitationToSave)

                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(Exception("Failed to send project invitation: ${e.message}"))
                }
            }

            private suspend fun sendProjectInvitationNotification(invitation: ProjectInvitation) {
                try {
                    val projectDoc = firestore.collection("projects")
                        .document(invitation.projectId)
                        .get()
                        .await()
                    val projectName = projectDoc.getString("title") ?: "a project"

                    val senderDoc = firestore.collection("users")
                        .document(invitation.fromUserId)
                        .get()
                        .await()
                    val senderName = "${senderDoc.getString("firstName") ?: ""} ${senderDoc.getString("lastName") ?: ""}".trim()
                        .takeIf { it.isNotBlank() }
                        ?: senderDoc.getString("email")?.split("@")?.first()
                        ?: "A user"

                    val data = hashMapOf(
                        "recipientUserId" to invitation.toUserId,
                        "projectId" to invitation.projectId,
                        "projectName" to projectName,
                        "fromUserId" to invitation.fromUserId,
                        "fromUserName" to senderName,
                        "invitationId" to invitation.invitationId
                    )

                    Firebase.functions("europe-west1").getHttpsCallable("sendProjectInvitationNotification")
                        .call(data)
                        .await()
                } catch (e: Exception) {
                }
            }

            suspend fun getPendingProjectInvitations(userId: String): List<ProjectInvitation> =
                try {
                    listenerManager.addActiveQuery("collectionGroup:projectInvitations for user: $userId")

                    val query = firestore.collectionGroup("projectInvitations")
                        .whereEqualTo("status", "pending")
                        .whereEqualTo("toUserId", userId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(30)

                    val snapshot = query.get().await()

                    snapshot.documents.mapNotNull { doc ->
                        ProjectInvitation(
                            invitationId = doc.id,
                            projectId = doc.getString("projectId") ?: "",
                            fromUserId = doc.getString("fromUserId") ?: "",
                            toUserId = doc.getString("toUserId") ?: "",
                            status = doc.getString("status") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                        )
                    }
                } catch (e: Exception) {
                    throw Exception("Failed to fetch invitations: ${e.message}")
                }

            suspend fun acceptInvitation(
                invitationId: String,
                projectId: String,
                userId: String
            ): Result<Unit> = try {
                listenerManager.addActiveQuery("transaction:acceptInvitation:$invitationId")

                val invitationRef = firestore.collection("projects")
                    .document(projectId)
                    .collection("projectInvitations")
                    .document(invitationId)

                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""
                val email = userDoc.getString("email") ?: ""
                val displayName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                    "$firstName $lastName".trim()
                } else {
                    email.ifEmpty { userId.take(8) }
                }

                firestore.runTransaction { transaction ->
                    val invitationSnapshot = transaction.get(invitationRef)
                    val status = invitationSnapshot.getString("status") ?: ""

                    if (status != "pending") {
                        throw Exception("Invitation already processed")
                    }

                    transaction.update(invitationRef, "status", "accepted")

                    val memberRef = firestore.collection("projects")
                        .document(projectId)
                        .collection("teamMembers")
                        .document(userId)

                    transaction.set(memberRef, mapOf(
                        "userId" to userId,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "displayName" to displayName,
                        "email" to email,
                        "joinedAt" to FieldValue.serverTimestamp()
                    ))

                    transaction.delete(invitationRef)
                }.await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to accept invitation: ${e.message}"))
            }

            suspend fun rejectInvitation(
                invitationId: String,
                projectId: String
            ): Result<Unit> = try {
                listenerManager.addActiveQuery("transaction:rejectInvitation:$invitationId")

                val invitationRef = firestore.collection("projects")
                    .document(projectId)
                    .collection("projectInvitations")
                    .document(invitationId)

                firestore.runTransaction { transaction ->
                    val invitationSnapshot = transaction.get(invitationRef)
                    val status = invitationSnapshot.getString("status") ?: ""

                    if (status != "pending") {
                        throw Exception("Invitation already processed")
                    }

                    transaction.delete(invitationRef)
                }.await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to reject invitation: ${e.message}"))
            }
        }