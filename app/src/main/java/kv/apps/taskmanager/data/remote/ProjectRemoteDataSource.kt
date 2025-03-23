package kv.apps.taskmanager.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.domain.model.ProjectRequestStatus
import javax.inject.Inject

class ProjectRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getAllProjectsForUser(): List<Project> = withContext(Dispatchers.IO) {
        val projects = mutableListOf<Project>()
        val currentUserId = currentUserId ?: throw Exception("User not logged in")

        try {
            val snapshot = firestore.collection("projects")
                .whereEqualTo("createdBy", currentUserId)
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.data?.let {
                    projects.add(
                        Project(
                            id = doc.id,
                            title = it["title"] as? String ?: "",
                            description = it["description"] as? String ?: "",
                            createdBy = it["createdBy"] as? String ?: "",
                            isCompleted = it["isCompleted"] as? Boolean ?: false,
                            dueDate = it["dueDate"] as? String ?: "",
                            teamMembers = it["teamMembers"] as? List<String> ?: emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch projects: ${e.message}")
        }

        return@withContext projects
    }
    suspend fun getAllProjects(): List<Project> = withContext(Dispatchers.IO) {
        val projects = mutableListOf<Project>()

        try {
            val snapshot = firestore.collection("projects")
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.data?.let {
                    projects.add(
                        Project(
                            id = doc.id,
                            title = it["title"] as? String ?: "",
                            description = it["description"] as? String ?: "",
                            createdBy = it["createdBy"] as? String ?: "",
                            isCompleted = it["isCompleted"] as? Boolean ?: false,
                            dueDate = it["dueDate"] as? String ?: "",
                            teamMembers = it["teamMembers"] as? List<String> ?: emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch projects: ${e.message}")
        }

        return@withContext projects
    }

    suspend fun getProjectById(projectId: String): Project? = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("projects")
                .document(projectId)
                .get()
                .await()

            return@withContext snapshot.data?.let { data ->
                Project(
                    id = snapshot.id,
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    createdBy = data["createdBy"] as? String ?: "",
                    isCompleted = data["isCompleted"] as? Boolean ?: false,
                    dueDate = data["dueDate"] as? String ?: "",
                    teamMembers = data["teamMembers"] as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch project: ${e.message}")
        }
    }

    suspend fun addTeamMembersToProject(projectId: String, teamMemberIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val project = getProjectById(projectId)
                ?: return@withContext Result.failure(Exception("Project not found"))

            val updatedTeamMembers = (project.teamMembers + teamMemberIds).distinct()

            val data = mapOf(
                "teamMembers" to updatedTeamMembers
            )

            firestore.collection("projects")
                .document(projectId)
                .update(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to add team members: ${e.message}"))
        }
    }

    suspend fun removeTeamMembersFromProject(projectId: String, teamMemberId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val project = getProjectById(projectId)
                ?: return@withContext Result.failure(Exception("Project not found"))

            val updatedTeamMembers = project.teamMembers - teamMemberId

            val data = mapOf(
                "teamMembers" to updatedTeamMembers
            )

            firestore.collection("projects")
                .document(projectId)
                .update(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to remove team member: ${e.message}"))
        }
    }

    suspend fun getTeamMembersForProject(projectId: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val project = getProjectById(projectId)
                ?: return@withContext Result.failure(Exception("Project not found"))

            Result.success(project.teamMembers)
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
                "dueDate" to project.dueDate,
                "teamMembers" to project.teamMembers
            )

            val documentReference = firestore.collection("projects")
                .add(data)
                .await()

            return@withContext documentReference.id
        } catch (e: Exception) {
            throw Exception("Failed to create project: ${e.message}")
        }
    }

    suspend fun deleteProject(projectId: String): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.collection("projects")
                .document(projectId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to delete project: ${e.message}")
        }
    }

    suspend fun updateProject(projectId: String, project: Project): Unit = withContext(Dispatchers.IO) {
        try {
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
        } catch (e: Exception) {
            throw Exception("Failed to update project: ${e.message}")
        }
    }

    suspend fun sendProjectInvitation(invitation: ProjectInvitation): Unit = withContext(Dispatchers.IO) {
        try {
            firestore.collection("projectInvitations")
                .document(invitation.invitationId)
                .set(invitation)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to send project invitation: ${e.message}")
        }
    }

    suspend fun getPendingProjectInvitations(toUserId: String): List<ProjectInvitation> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("projectInvitations")
                .whereEqualTo("toUserId", toUserId)
                .whereEqualTo("status", ProjectRequestStatus.PENDING.name)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                document.toObject(ProjectInvitation::class.java)
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch pending project invitations: ${e.message}")
        }
    }

    suspend fun acceptInvitation(
        invitationId: String,
        projectId: String,
        userId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Update the invitation status to ACCEPTED
            val updateData = mapOf(
                "status" to ProjectRequestStatus.ACCEPTED.name
            )
            firestore.collection("projectInvitations")
                .document(invitationId)
                .update(updateData)
                .await()

            // Add the user to the project's teamMembers
            val addTeamMembersResult = addTeamMembersToProject(projectId, listOf(userId))
            if (addTeamMembersResult.isFailure) {
                return@withContext addTeamMembersResult
            }

            // Delete the invitation after accepting
            firestore.collection("projectInvitations")
                .document(invitationId)
                .delete()
                .await()

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
            // Update the invitation status to REJECTED
            val updateData = mapOf(
                "status" to ProjectRequestStatus.REJECTED.name
            )
            firestore.collection("projectInvitations")
                .document(invitationId)
                .update(updateData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to reject invitation: ${e.message}"))
        }
    }
}