package kv.apps.taskmanager.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import javax.inject.Inject

class ProjectRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private suspend fun fetchTeamMembers(projectId: String): List<String> = try {
                firestore.collection("projects")
                    .document(projectId)
                    .collection("teamMembers")
                    .get()
                    .await()
                    .documents
                    .map { it.id }
            } catch (_: Exception) {
                emptyList()
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

            val allProjectIds = (createdProjects.documents.map { it.id } + memberProjects).distinct()

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
    ): Result<Unit> =
        try {
            val batch = firestore.batch()
            for (memberId in teamMemberIds) {
                val (firstName, lastName) = getUserDetails(memberId)
                val memberRef = firestore.collection("projects")
                    .document(projectId)
                    .collection("teamMembers")
                    .document(memberId)
                batch.set(
                    memberRef, mapOf(
                        "userId" to memberId,
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "joinedAt" to FieldValue.serverTimestamp()
                    )
                )
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
                Project(
                    id = doc.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    createdBy = data["createdBy"] as? String ?: "",
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

    suspend fun getTeamMembersForProject(projectId: String): Result<List<String>> =
            try {
                Result.success(fetchTeamMembers(projectId))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch team members: ${e.message}"))
            }

    suspend fun createProject(project: Project): String =
        try {
            val currentUserId = currentUserId ?: throw Exception("User not logged in")
            val data = mapOf(
                "title" to project.title,
                "description" to project.description,
                "createdBy" to currentUserId,
                "isCompleted" to project.isCompleted,
                "dueDate" to project.dueDate
            )

            val projectId = project.id.ifEmpty {
                firestore.collection("projects").document().id
            }

            firestore.collection("projects")
                .document(projectId)
                .set(data)
                .await()

            projectId
        } catch (e: Exception) {
            throw Exception("Failed to create project: ${e.message}")
        }

    suspend fun deleteProject(projectId: String) {
        try {
            deleteSubcollection("projects/$projectId/teamMembers")
            deleteSubcollection("projects/$projectId/projectInvitations")
            firestore.collection("projects")
                .document(projectId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to delete project: ${e.message}")
        }
    }

    private suspend fun deleteSubcollection(path: String) {
        val collectionRef = firestore.collection(path)

        while (true) {
            val documents = collectionRef.limit(500).get().await()
            if (documents.isEmpty) return

            val batch = firestore.batch()
            documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
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


    suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit> =
            try {
                val invitationId = if (invitation.invitationId.isEmpty())
                    firestore.collection("projects")
                        .document(invitation.projectId)
                        .collection("projectInvitations")
                        .document().id
                else invitation.invitationId

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

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to send project invitation: ${e.message}"))
            }

    suspend fun getPendingProjectInvitations(userId: String): List<ProjectInvitation> =
            try {
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

                transaction.update(invitationRef, "status", "accepted")

                val memberRef = firestore.collection("projects")
                    .document(projectId)
                    .collection("teamMembers")
                    .document(userId)

                transaction.set(memberRef, mapOf(
                    "userId" to userId,
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
            firestore.collection("projects")
                .document(projectId)
                .collection("projectInvitations")
                .document(invitationId)
                .update("status", "rejected")
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reject invitation: ${e.message}"))
        }
    }