package kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables

import androidx.annotation.ColorRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kv.apps.taskmanager.R
import java.time.LocalDate
import kotlin.math.ceil

@Composable
fun rememberCustomDatePicker(
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    @ColorRes headerColor: Int = R.color.main_app_color,
    @ColorRes headerTextColor: Int = R.color.black,
    @ColorRes backgroundColor: Int = R.color.background_color,
    @ColorRes buttonColor: Int = R.color.main_app_color,
    @ColorRes dayTextColor: Int = R.color.white
): () -> Unit {
    val dialogState = rememberMaterialDialogState()
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        CustomMaterialDatePicker(
            dialogState = dialogState,
            initialDate = initialDate,
            onDateSelected = { date ->
                onDateSelected(date)
                showDialog.value = false
            },
            onDismiss = { showDialog.value = false },
            headerColor = headerColor,
            headerTextColor = headerTextColor,
            backgroundColor = backgroundColor,
            buttonColor = buttonColor,
            dayTextColor = dayTextColor
        )
    }

    return {
        showDialog.value = true
    }
}

@Composable
fun CustomMaterialDatePicker(
    dialogState: MaterialDialogState,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    @ColorRes headerColor: Int = R.color.main_app_color,
    @ColorRes headerTextColor: Int = R.color.black,
    @ColorRes backgroundColor: Int = R.color.background_color,
    @ColorRes buttonColor: Int = R.color.main_app_color,
    @ColorRes dayTextColor: Int = R.color.white
) {
    val selectedDate = remember { mutableStateOf(initialDate ?: LocalDate.now()) }

    LaunchedEffect(Unit) {
        dialogState.show()
    }

    MaterialDialog(
        dialogState = dialogState,
        onCloseRequest = {
            dialogState.hide()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        ),
        buttons = {
            positiveButton(
                text = "OK",
                textStyle = TextStyle(color = colorResource(buttonColor))
            ) {
                onDateSelected(selectedDate.value)
                dialogState.hide()
                onDismiss()
            }
            negativeButton(
                text = "Cancel",
                textStyle = TextStyle(color = colorResource(buttonColor))
            ) {
                dialogState.hide()
                onDismiss()
            }
        },
        backgroundColor = colorResource(backgroundColor),
        shape = RoundedCornerShape(12.dp),
        autoDismiss = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(headerColor))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select Date",
                    color = colorResource(headerTextColor),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                )
            }

            CustomDateSelector(
                selectedDate = selectedDate.value,
                onDateSelected = { selectedDate.value = it },
                headerColor = headerColor,
                headerTextColor = headerTextColor,
                buttonColor = buttonColor,
                dayTextColor = dayTextColor
            )
        }
    }
}

@Composable
fun CustomDateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    @ColorRes headerColor: Int,
    @ColorRes headerTextColor: Int,
    @ColorRes buttonColor: Int,
    @ColorRes dayTextColor: Int
) {
    val yearRange = 1920..2100
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(buttonColor)
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = months[selectedDate.monthValue - 1],
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Month",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(colorResource(headerColor))
                        .height(200.dp)
                ) {
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(
                            onClick = {
                                onDateSelected(selectedDate.withMonth(index + 1))
                                expanded = false
                            },
                            text = {
                                Text(
                                    text = month,
                                    color = if (selectedDate.monthValue == index + 1) {
                                        colorResource(buttonColor)
                                    } else {
                                        colorResource(headerTextColor)
                                    },
                                    fontWeight = if (selectedDate.monthValue == index + 1) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }

            var yearExpanded by remember { mutableStateOf(false) }
            Box {
                TextButton(
                    onClick = { yearExpanded = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(buttonColor)
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = selectedDate.year.toString(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Year",
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                DropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false },
                    modifier = Modifier
                        .background(colorResource(headerColor))
                        .height(200.dp)
                ) {
                    val currentYear = 2025
                    val visibleYears = yearRange.filter {
                        it >= 1920 && it <= currentYear + 20
                    }

                    visibleYears.forEach { year ->
                        DropdownMenuItem(
                            onClick = {
                                onDateSelected(selectedDate.withYear(year))
                                yearExpanded = false
                            },
                            text = {
                                Text(
                                    text = year.toString(),
                                    color = if (selectedDate.year == year) {
                                        colorResource(buttonColor)
                                    } else {
                                        colorResource(headerTextColor)
                                    },
                                    fontWeight = if (selectedDate.year == year) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val firstDayOfMonth = selectedDate.withDayOfMonth(1)
        val daysInMonth = selectedDate.lengthOfMonth()
        val startOffset = firstDayOfMonth.dayOfWeek.value % 7

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(buttonColor),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val weeks = ceil((startOffset + daysInMonth) / 7.0).toInt()

        Column {
            for (week in 0 until weeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (day in 0 until 7) {
                        val dayNumber = week * 7 + day - startOffset + 1
                        val isCurrentMonth = dayNumber in 1..daysInMonth
                        val isSelected = isCurrentMonth && dayNumber == selectedDate.dayOfMonth

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            color = colorResource(buttonColor),
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable(
                                    enabled = isCurrentMonth,
                                    onClick = {
                                        if (isCurrentMonth) {
                                            onDateSelected(selectedDate.withDayOfMonth(dayNumber))
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCurrentMonth) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = if (isSelected) {
                                        colorResource(headerTextColor)
                                    } else {
                                        colorResource(dayTextColor)
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}