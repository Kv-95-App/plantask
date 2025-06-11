package kv.apps.taskmanager.presentation.screens.projectScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProjectMembers(
    projectId: String,
    onBackPressed: () -> Unit,
    projectViewModel: ProjectViewModel,
    navController: NavController
) {
    val teamMembersWithDetails by projectViewModel.teamMembersWithDetails.collectAsState()
    val loading by projectViewModel.teamMembersLoading.collectAsState()
    val error by projectViewModel.teamMembersError.collectAsState()

    LaunchedEffect(projectId) {
        projectViewModel.fetchTeamMembersForProject(projectId)
    }

    Scaffold(
        topBar = {
            TopBar(
                onBackPressed = onBackPressed,
                onProfileClicked = { navController.navigate("profile") },
                onLogoutClicked = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                showBackArrow = true,
                navController = navController
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = error!!, color = Color.Red)
                }
            } else {
                Text(
                    text = "Team Members (${teamMembersWithDetails.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = mainAppColor
                )

                if (teamMembersWithDetails.isEmpty()) {
                    Text("No team members yet")
                } else {
                    LazyColumn {
                        items(teamMembersWithDetails) { member ->
                            MemberItem(
                                name = "${member.firstName} ${member.lastName}".trim(),
                                email = member.email ?: member.userId.take(8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberItem(
    name: String,
    email: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = onGoingCardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}