package kv.apps.taskmanager.presentation.screens.utilScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kv.apps.taskmanager.R
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun GetStartedScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.plantask_transparent),
                contentDescription = "Task Management",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(470.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 80.dp)
            ) {
                Text(
                    text = "Manage your Tasks with",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PlanTask",
                    color = mainAppColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { navController.navigate("login") },
                    colors = ButtonDefaults.buttonColors(containerColor = mainAppColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text("Get Started", color = Color.Black, fontSize = 20.sp)
                }
            }
        }
    }
}