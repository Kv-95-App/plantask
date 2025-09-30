package kv.apps.taskmanager.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kv.apps.taskmanager.data.connectivity.ConnectivityObserver
import kv.apps.taskmanager.data.connectivity.NetworkStatus
import kv.apps.taskmanager.data.local.dao.ProjectDao
import kv.apps.taskmanager.data.local.dao.TaskDao
import javax.inject.Inject

@HiltViewModel
class OfflineStatusViewModel @Inject constructor(
    private val connectivityObserver: ConnectivityObserver,
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao
) : ViewModel() {

    private val _networkStatus = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount

    init {
        viewModelScope.launch {
            connectivityObserver.observe().collect { status ->
                _networkStatus.value = status
            }
        }

        // Monitor pending sync items
        viewModelScope.launch {
            while (true) {
                updatePendingSyncCount()
                kotlinx.coroutines.delay(5000) // Check every 5 seconds
            }
        }
    }

    private suspend fun updatePendingSyncCount() {
        try {
            val tasksNeedingSync = taskDao.getTasksNeedingSync().size
            val projectsNeedingSync = projectDao.getProjectsNeedingSync().size
            _pendingSyncCount.value = tasksNeedingSync + projectsNeedingSync
        } catch (e: Exception) {
            // Ignore errors in counting
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineStatusIndicator(
    modifier: Modifier = Modifier,
    viewModel: OfflineStatusViewModel = hiltViewModel()
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

    val (icon, color, text) = when (networkStatus) {
        NetworkStatus.AVAILABLE -> Triple(
            Icons.Default.Wifi,
            Color(0xFF4CAF50),
            "Online"
        )

        NetworkStatus.UNAVAILABLE, NetworkStatus.LOST -> Triple(
            Icons.Default.CloudOff,
            Color(0xFFFF9800),
            "Offline"
        )

        NetworkStatus.LOSING -> Triple(
            Icons.Default.SyncProblem,
            Color(0xFFFF5722),
            "Losing Connection"
        )
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            if (pendingSyncCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Pending sync",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$pendingSyncCount",
                        color = Color(0xFF2196F3),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}