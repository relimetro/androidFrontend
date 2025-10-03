package com.example.groupproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.groupproject.ui.theme.GroupProjectTheme
import kotlin.io.encoding.Base64

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Notification : Screen("notifications")
}

data class NavItem(
    var label: String,
    val icon: ImageVector,
    val screen: Screen
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroupProjectTheme {
                Mainfunction()
            }
        }
    }
}

@Composable
fun HomeScreen( modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ){
        Row( modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, bottom = 100.dp,
                        start = 50.dp, end = 50.dp),
            ) {
            Text(text ="Home Screen",
                fontSize = 36.sp,
                color = Color.Black)
        }
        Row {
            Text(text ="This is sample Text",
                fontSize = 36.sp,
                color = Color.Black)
        }
    }
}
@Composable
fun QuestionScreen( modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ){
        Row( modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, bottom = 100.dp,
                start = 50.dp, end = 50.dp),
        ) {
            Text(
                text = "Question Screen",
                fontSize = 36.sp,
                color = Color.Black)
        }
        Row {
            Text(text ="This is where you'll be able to answer the question for your diagnosis",
                fontSize = 30.sp,
                color = Color.Black)
        }
    }
}
@Composable
fun NotificationScreen( modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 50.dp, bottom = 100.dp,
                    start = 50.dp, end = 50.dp
                ),
        ) {
            Text(
                text = "Notifications Screen",
                fontSize = 36.sp,
                color = Color.Black
            )
        }

        Row {
            Text(
                text = "This is where notifications about your diagnosis will come in.",
                fontSize = 36.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun Mainfunction() {
    val navController = rememberNavController()
    val navItemList = listOf(
        NavItem(label = "Home", icon = Icons.Default.Home, screen = Screen.Home),
        NavItem(label = "Questionnaire", icon = Icons.Default.AccountBox, screen = Screen.Settings),
        NavItem(label = "Notification", icon = Icons.Default.Notifications, screen =
            Screen.Notification)
    )
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var isDialogOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar {
                NavigationBar {
                    navItemList.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                selectedIndex = index
                                if (navController.currentDestination?.route != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        launchSingleTop =
                                            true // Avoid multiple copies of the same destination
                                        restoreState =
                                            true // Restore previous state when navigating back
                                    }
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription =
                                item.label) },
                            label = { Text(text = item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
// Set up the navigation graph
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Settings.route) { QuestionScreen() }
            composable(Screen.Notification.route) { NotificationScreen() }
        }
    }
}
