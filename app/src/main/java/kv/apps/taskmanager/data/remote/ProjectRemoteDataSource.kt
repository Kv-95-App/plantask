package kv.apps.taskmanager.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import javax.inject.Inject
import com.google.firebase.Timestamp

class ProjectRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    private suspend fun fetchTeamMembers(projectId: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
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
        }

    private suspend fun getUserDetails(userId: String): Pair<String, String> =
        withContext(Dispatchers.IO) {
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
        }

    suspend fun addTeamMembersToProject(
        projectId: String,
        teamMemberIds: List<String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
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
    }

    suspend fun getAllProjectsForUser(): List<Project> = withContext(Dispatchers.IO) {
        val currentUserId = currentUserId ?: throw Exception("User not logged in")
        val projects = mutableListOf<Project>()
        try {
            val createdProjects = firestore.collection("projects")
                .whereEqualTo("createdBy", currentUserId)
                .get()
                .await()
            val memberProjects = firestore.collectionGroup("teamMembers")
                .whereEqualTo("__name__", currentUserId)
                .get()
                .await()
                .documents
                .mapNotNull { it.reference.parent.parent?.id }
            val allProjectIds =
                (createdProjects.documents.map { it.id } + memberProjects).distinct()
            allProjectIds.forEach { projectId ->
                val projectDoc = firestore.collection("projects").document(projectId).get().await()
                projectDoc.data?.let { data ->
                    projects.add(
                        Project(
                            id = projectDoc.id,
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            createdBy = data["createdBy"] as? String ?: "",
                            isCompleted = data["isCompleted"] as? Boolean ?: false,
                            dueDate = data["dueDate"] as? String ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch projects: ${e.message}")
        }
        projects
    }

    suspend fun getAllProjects(): List<Project> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("projects")
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.data?.let { data ->
                        Project(
                            id = doc.id,
                            title = data["title"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            createdBy = data["createdBy"] as? String ?: "",
                            isCompleted = data["isCompleted"] as? Boolean ?: false,
                            dueDate = data["dueDate"] as? String ?: "",
                            teamMembers = fetchTeamMembers(doc.id)
                        )
                    }
                }
        } catch (e: Exception) {
            throw Exception("Failed to fetch projects: ${e.message}")
        }
    }

    suspend fun getProjectById(projectId: String): Project? = withContext(Dispatchers.IO) {
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
                    isCompleted = data["isCompleted"] as? Boolean ?: false,
                    dueDate = data["dueDate"] as? String ?: "",
                    teamMembers = fetchTeamMembers(doc.id)
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch project: ${e.message}")
        }
    }

    suspend fun removeTeamMembersFromProject(
        projectId: String,
        teamMemberId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
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
    }

    suspend fun getTeamMembersForProject(projectId: String): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(fetchTeamMembers(projectId))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to fetch team members: ${e.message}"))
            }
        }

    suspend fun createProject(project: Project): String = withContext(Dispatchers.IO) {
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
                // Only generate a new ID if none was provided
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
    }

    suspend fun deleteProject(projectId: String): Unit = withContext(Dispatchers.IO) {
        try {
            val members = fetchTeamMembers(projectId)
            if (members.isNotEmpty()) {
                val batch = firestore.batch()
                members.forEach { memberId ->
                    val memberRef = firestore.collection("projects")
                        .document(projectId)
                        .collection("teamMembers")
                        .document(memberId)
                    batch.delete(memberRef)
                }
                batch.commit().await()
            }
            firestore.collection("projects")
                .document(projectId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to delete project: ${e.message}")
        }
    }

    suspend fun updateProject(projectId: String, project: Project): Unit =
        withContext(Dispatchers.IO) {
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
        }

    suspend fun sendProjectInvitation(invitation: ProjectInvitation): Result<Unit> = withContext(Dispatchers.IO) {
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
    }



    suspend fun getPendingProjectInvitations(userId: String): List<ProjectInvitation> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collectionGroup("projectInvitations")
                    .whereEqualTo("toUserId", userId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(ProjectInvitation::class.java)?.copy(invitationId = doc.id)
                    }
            } catch (e: Exception) {
                throw Exception("Failed to fetch invitations: ${e.message}")
            }
        }

    suspend fun acceptInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("projects")
                .document(projectId)
                .collection("projectInvitations")
                .document(invitationId)
                .update("status", "accepted")
                .await()

            addTeamMembersToProject(projectId, listOf(userId))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to accept invitation: ${e.message}"))
        }
    }

    suspend fun rejectInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
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
}
