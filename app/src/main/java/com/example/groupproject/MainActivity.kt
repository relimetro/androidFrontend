package com.example.groupproject

import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.backend.backend
import com.example.backend.BErr
import com.example.backend.Gender
import com.example.backend.NutritionDiet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


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
    // for backend testing (can remove if you want)
    var testHttp: String by remember { mutableStateOf("")} // sample variable to show output to UI
    val ctx = LocalContext.current // need to pass "context" as argument when calling request
	var testIP by rememberSaveable { mutableStateOf(value="localhost")}

    Box(modifier
        .fillMaxSize()
        .background(Color.White),
        contentAlignment = Alignment.Center
    ){
        Row( modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 50.dp, bottom = 100.dp,
                start = 50.dp, end = 50.dp
            ),
            ) {
            Text(text ="Home Screen",
                fontSize = 36.sp,
                color = Color.Black)
        }
        Row {
            Text(text ="This is sample Text: $testHttp",
                fontSize = 36.sp,
                color = Color.Black)

            // For backend testing, can remove if want
            Button(onClick={
                // request hello message from backend given (LocalContext.current, and name); (see backend.kt for documentation)
                backend.request_hello(ctx, "Conor") { resp -> // anonymous function is called when backend responds
                    when(resp.err){ // switch statement
                        BErr.Ok -> testHttp = resp.message
                        BErr.Not_Signed_In -> testHttp = "No Connection / Not Signed In"
                        BErr.Exception -> testHttp = resp.message
                    }
            } }){ Text("HTTP")}
			TextField(value = testIP, onValueChange = { testIP = it}, label = {Text("Ip address")})
            Button(onClick={ backend.setAddresss(testIP) } ){ Text("change ip")}

        }
    }
}
@Composable
fun QuestionScreen( modifier: Modifier = Modifier) {
    var name = rememberSaveable { mutableStateOf("") }
    val Diabetic = rememberSaveable { mutableStateOf("Yes") }
    var alcohol = rememberSaveable { mutableStateOf("") }
    var heartRate = rememberSaveable { mutableStateOf("") }
    var BloodOxygenLevel = rememberSaveable { mutableStateOf("")}
    var BodyTemperature = rememberSaveable { mutableStateOf("") }
    var Weight = rememberSaveable { mutableStateOf("") }
    var MRI_Delay = rememberSaveable { mutableStateOf("") }
    var Prescription = rememberSaveable { mutableStateOf("") }
    var Age = rememberSaveable { mutableStateOf("") }
    var EducationLevel = rememberSaveable { mutableStateOf("") }
    var DominantHand = rememberSaveable { mutableStateOf("Right") }
    var Gender = rememberSaveable { mutableStateOf("Male") }
    var FamilyHistory = rememberSaveable { mutableStateOf("Yes") }
    var APOEE4 = rememberSaveable { mutableStateOf("Yes") }
    var PhysicalActivity = rememberSaveable { mutableStateOf("Yes") }
    var DepressionStatus = rememberSaveable { mutableStateOf("Yes") }
    var MedicationHistory = rememberSaveable { mutableStateOf("Yes") }
    var NutrientDiet = rememberSaveable { mutableStateOf("Yes") }
    var SleepQuality  = rememberSaveable { mutableStateOf("Good") }
    var ChronicHealthConditions = rememberSaveable { mutableStateOf("Yes") }


    Box(modifier
        .fillMaxSize()
        .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = 50.dp, bottom = 100.dp,
                    start = 50.dp, end = 50.dp
                ),
        ) {
            Text(
                text = "Question Screen",
                fontSize = 36.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(60.dp))

            TextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Enter your name") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            Text("Diabetic")

            RadioButton(
                selected = Diabetic.value == "Yes",
                onClick = { Diabetic.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = Diabetic.value == "No",
                onClick = { Diabetic.value = "No" }
            )
            Text("No")

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = alcohol.value,
                onValueChange = { alcohol.value = it },
                label = { Text("Enter your alcohol level") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = heartRate.value,
                onValueChange = { heartRate.value = it },
                label = { Text("Enter your heart rate") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = BloodOxygenLevel.value,
                onValueChange = { BloodOxygenLevel.value = it },
                label = { Text("Enter your blood oxygen level") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = BodyTemperature.value,
                onValueChange = { BodyTemperature.value = it },
                label = { Text("Enter your body temperature") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = Weight.value,
                onValueChange = { Weight.value = it },
                label = { Text("Enter your weight") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = MRI_Delay.value,
                onValueChange = { MRI_Delay.value = it },
                label = { Text("Enter your MRI delay") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = Prescription.value,
                onValueChange = { Prescription.value = it },
                label = { Text("Enter your prescription") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = Age.value,
                onValueChange = { Age.value = it },
                label = { Text("Enter your age") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = EducationLevel.value,
                onValueChange = { EducationLevel.value = it },
                label = { Text("Enter your Education Level") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            Text("Dominant Hand?")

            RadioButton(
                selected = DominantHand.value == "Right",
                onClick = { DominantHand.value = "Right" }
            )
            Text("Right")
            RadioButton(
                selected = DominantHand.value == "Left",
                onClick = { DominantHand.value = "Left" }
            )
            Text("Left")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Gender?")

            RadioButton(
                selected = Gender.value == "Male",
                onClick = { Gender.value = "Male" }
            )
            Text("Male")
            RadioButton(
                selected = Gender.value == "Female",
                onClick = { Gender.value = "Female" }
            )
            Text("Female")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Family History?")

            RadioButton(
                selected = FamilyHistory.value == "Yes",
                onClick = { FamilyHistory.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = FamilyHistory.value == "No",
                onClick = { FamilyHistory.value = "No" }
            )
            Text("No")


            Spacer(modifier = Modifier.height(30.dp))

            Text("APOEE4?")

            RadioButton(
                selected = APOEE4.value == "Yes",
                onClick = { APOEE4.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = APOEE4.value == "No",
                onClick = { APOEE4.value = "No" }
            )
            Text("No")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Physical Activity?")

            RadioButton(
                selected = PhysicalActivity.value == "Yes",
                onClick = { PhysicalActivity.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = PhysicalActivity.value == "No",
                onClick = { PhysicalActivity.value = "No" }
            )
            Text("No")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Depression Status?")

            RadioButton(
                selected = DepressionStatus.value == "Yes",
                onClick = { DepressionStatus.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = DepressionStatus.value == "No",
                onClick = { DepressionStatus.value = "No" }
            )
            Text("No")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Medication History?")

            RadioButton(
                selected = MedicationHistory.value == "Yes",
                onClick = { MedicationHistory.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = MedicationHistory.value == "No",
                onClick = { MedicationHistory.value = "No" }
            )
            Text("No")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Nutrient Diet?")

            RadioButton(
                selected = NutrientDiet.value == "Yes",
                onClick = { NutrientDiet.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = NutrientDiet.value == "No",
                onClick = { NutrientDiet.value = "No" }
            )
            Text("No")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Sleep Quality?")

            RadioButton(
                selected = SleepQuality.value == "Good",
                onClick = { SleepQuality.value = "Good" }
            )
            Text("Good")
            RadioButton(
                selected = SleepQuality.value == "Bad",
                onClick = { SleepQuality.value = "Good" }
            )
            Text("Bad")

            Spacer(modifier = Modifier.height(30.dp))

            Text("Chronic Health Conditions?")

            RadioButton(
                selected = ChronicHealthConditions.value == "Yes",
                onClick = { ChronicHealthConditions.value = "Yes" }
            )
            Text("Yes")
            RadioButton(
                selected = ChronicHealthConditions.value == "No",
                onClick = { ChronicHealthConditions.value = "No" }
            )
            Text("No")

        }
    }

}
@Composable
fun NotificationScreen( modifier: Modifier = Modifier) {
    Box(modifier
        .fillMaxSize()
        .background(Color.White),
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
