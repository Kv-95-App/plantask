package kv.apps.taskmanager.presentation.screens.taskSection.taskComposables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberSelectionModal(
    showMemberSelection: Boolean,
    onDismiss: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedUsers: Set<String>,
    onUserSelected: (String) -> Unit,
    filteredMembers: List<TeamMember>,
    onConfirmSelection: () -> Unit,
    assignedMembers: Set<String>,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val availableMembers = remember(filteredMembers, assignedMembers) {
        filteredMembers.filterNot { member ->
            assignedMembers.contains(member.userId)
        }
    }

    if (showMemberSelection) {
        ModalBottomSheet(
            onDismissRequest = {
                focusManager.clearFocus()
                onDismiss()
            },
            sheetState = sheetState,
            containerColor = backgroundColor,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFF3A3E4B), RoundedCornerShape(2.dp))
                )
            },
            modifier = modifier.fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        focusManager.clearFocus()
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Assign Task To",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "${selectedUsers.size} selected",
                            color = mainAppColor,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Empty to prevent propagation */ }
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search team members", color = Color(0xFF6D7187)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF6D7187)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF3A3E4B),
                                unfocusedContainerColor = Color(0xFF3A3E4B),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = mainAppColor,
                                focusedIndicatorColor = mainAppColor,
                                unfocusedIndicatorColor = Color(0xFF3A3E4B)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    if (availableMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "No members",
                                    tint = Color(0xFF3A3E4B),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        "No available members match your search"
                                    } else {
                                        "All team members are already assigned"
                                    },
                                    color = Color(0xFF6D7187),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableMembers) { member ->
                                val isSelected = selectedUsers.contains(member.userId)
                                val borderColor by animateColorAsState(
                                    targetValue = if (isSelected) mainAppColor else Color.Transparent,
                                    label = "borderColor"
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUserSelected(member.userId) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF3A3E4B),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = borderColor
                                    ),
                                    tonalElevation = if (isSelected) 4.dp else 0.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    color = if (isSelected) mainAppColor else Color(0xFF2C2F38),
                                                    shape = CircleShape
                                                )
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.firstName.take(1) + member.lastName.take(1),
                                                color = if (isSelected) Color.Black else Color.White,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${member.firstName} ${member.lastName}",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            member.email?.let { email ->
                                                Text(
                                                    text = email,
                                                    color = Color(0xFF6D7187),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { onUserSelected(member.userId) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = mainAppColor,
                                                uncheckedColor = Color(0xFF6D7187)
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                delay(300)
                                onConfirmSelection()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mainAppColor,
                            contentColor = Color.Black
                        ),
                        enabled = selectedUsers.isNotEmpty()
                    ) {
                        Text(
                            text = "Assign to ${selectedUsers.size} member(s)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}