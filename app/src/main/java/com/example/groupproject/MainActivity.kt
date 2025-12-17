package com.example.groupproject

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.groupproject.ui.theme.GroupProjectTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.backend.backend
import com.example.backend.BErr
import com.example.backend.ChronicHealthConditions
import com.example.backend.DomHand
import com.example.backend.EducationLevel
import com.example.backend.Gender
import com.example.backend.LifestyleData
import com.example.backend.NutritionDiet
import com.example.backend.PhysicalActivity
import com.example.backend.Prescription
import com.example.backend.SleepQuality
import com.example.backend.SmokingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object QuestionnaireSelect: Screen("questionnaireSelect")
    object QuestionnaireC : Screen("questionnaireC")
    object Notification : Screen("notifications")
}

data class NavItem(
    var label: String,
    val icon: ImageVector,
    val screen: Screen
)

enum class AttentionMode { SERIAL_7S, WORLD_BACKWARD }

data class MmseState(
    // Orientation
    val orientationTime: List<String> = List(5) { "" },   // year, season, date, day, month
    val orientationPlace: List<String> = List(5) { "" },  // state, country, town, hospital, floor

    // Registration
    val registrationWordsShown: List<String> = List(3) { "" },
    val registrationImmediate: List<String> = List(3) { "" },

    // Attention & Calculation
    val attentionMode: AttentionMode = AttentionMode.SERIAL_7S,
    val serial7Answers: List<String> = List(5) { "" },
    val worldBackward: String = "",

    // Recall
    val recallAnswers: List<String> = List(3) { "" },

    // Language (up to repetition)
    val repetitionInput: String = ""
)

class MmseViewModel : ViewModel() {
    private val correctLocationAnswers = listOf(
        "Ireland", // Country
        "Munster",    // State
        "Cork",   // City/Town
        "Cork University Hospital", // Hospital
        "2nd Floor"      // Floor
    )

    private fun currentSeason(month: Int): String =
        when (month) {
            12, 1, 2 -> "Winter"
            3, 4, 5 -> "Spring"
            6, 7, 8 -> "Summer"
            else -> "Fall"
        }

    private val _state = MutableStateFlow(MmseState())
    val state: StateFlow<MmseState> = _state

    fun setOrientationTime(index: Int, value: String) = _state.update {
        it.copy(orientationTime = it.orientationTime.toMutableList().also { list -> list[index] = value })
    }

    fun setOrientationPlace(index: Int, value: String) = _state.update {
        it.copy(orientationPlace = it.orientationPlace.toMutableList().also { list -> list[index] = value })
    }

    fun setRegistrationWord(index: Int, value: String) = _state.update {
        it.copy(
            registrationWordsShown = it.registrationWordsShown
                .toMutableList()
                .also { list -> list[index] = value }
        )
    }

    fun setRegistrationImmediate(index: Int, value: String) = _state.update {
        it.copy(registrationImmediate = it.registrationImmediate.toMutableList().also { list -> list[index] = value })
    }

    fun setAttentionMode(mode: AttentionMode) = _state.update { it.copy(attentionMode = mode) }

    fun setSerial7(index: Int, value: String) = _state.update {
        it.copy(serial7Answers = it.serial7Answers.toMutableList().also { list -> list[index] = value })
    }

    fun setWorldBackward(value: String) = _state.update { it.copy(worldBackward = value) }

    fun setRecall(index: Int, value: String) = _state.update {
        it.copy(recallAnswers = it.recallAnswers.toMutableList().also { list -> list[index] = value })
    }

    fun setRepetition(value: String) = _state.update { it.copy(repetitionInput = value) }

    fun computeScore(): Int {
        val s = _state.value
        var score = 0

        /* ---------------- ORIENTATION: TIME (5) ---------------- */
        val today = LocalDateTime.now()

        val correctTimeAnswers = listOf(
            today.year.toString(),
            currentSeason(today.monthValue),
            today.dayOfMonth.toString(),
            today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
            today.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        )

        correctTimeAnswers.forEachIndexed { i, correct ->
            if (s.orientationTime[i].trim().equals(correct, ignoreCase = true)) {
                score++
            }
        }

        /* ---------------- ORIENTATION: PLACE (5) ---------------- */
        correctLocationAnswers.forEachIndexed { i, correct ->
            if (s.orientationPlace[i].trim().equals(correct, ignoreCase = true)) {
                score++
            }
        }

        /* ---------------- REGISTRATION (3) ---------------- */
        val registrationCorrect = s.registrationWordsShown
            .map { it.trim().lowercase() }
            .toSet()

        val registrationGiven = s.registrationImmediate
            .map { it.trim().lowercase() }
            .toSet()

        score += registrationCorrect.intersect(registrationGiven).size.coerceAtMost(3)

        /* ---------------- ATTENTION & CALCULATION (5) ---------------- */
        when (s.attentionMode) {
            AttentionMode.SERIAL_7S -> {
                val correctSerial7s = listOf("93", "86", "79", "72", "65")

                correctSerial7s.forEachIndexed { i, correct ->
                    if (s.serial7Answers.getOrNull(i)?.trim() == correct) {
                        score++
                    }
                }
            }

            AttentionMode.WORLD_BACKWARD -> {
                if (s.worldBackward.trim().equals("dlrow", ignoreCase = true)) {
                    score += 5
                }
            }
        }

        /* ---------------- RECALL (3) ---------------- */
        val recallGiven = s.recallAnswers
            .map { it.trim().lowercase() }
            .toSet()

        score += registrationCorrect.intersect(recallGiven).size.coerceAtMost(3)

        /* ---------------- LANGUAGE: REPETITION (1) ---------------- */
        if (s.repetitionInput.trim()
                .equals("No ifs, ands, or buts", ignoreCase = true)
        ) {
            score++
        }

        return score
    }

}



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

class UserViewModel: ViewModel() {
    var _username = MutableStateFlow("") // private, screens read using indes (not _index) and write using provided methods
    var username: StateFlow<String> = _username.asStateFlow()
}


@Composable
fun HomeScreen(uvm: UserViewModel = UserViewModel()) {
    // for backend testing (can remove if you want)
    var modifier = Modifier

    Box(modifier
        .fillMaxSize()
        .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(
                        top = 50.dp, bottom = 100.dp,
                        start = 50.dp, end = 50.dp
                    ),
            ) {
                Text(
                    text = "Home Screen",
                    fontSize = 36.sp,
                    color = Color.Black
                ) }




                Row(Modifier.fillMaxWidth().height(100.dp)) {
					var myRiskScore by rememberSaveable { mutableStateOf("...") }
                    val ctx = LocalContext.current
                    Text("Risk Score: $myRiskScore")
					Button(onClick = {
						backend.request_risk(ctx) { resp ->
								when(resp.err){
									BErr.Ok -> myRiskScore = resp.message
									BErr.Not_Signed_In -> Log.i("blah","not signed in or no internet") // redirect to login
									BErr.Exception -> Log.i("blah","exception") // other network issue
								} }
						}) { Text("calculate risk") }
					}

                Row(Modifier.fillMaxWidth().height(100.dp)) {
                    var myNews by rememberSaveable { mutableStateOf("...") }
					val ctx = LocalContext.current
                    Text("News: ${myNews}")
					Button(onClick = {
						backend.request_news(ctx) { resp ->
								when(resp.err){
									BErr.Ok -> myNews = resp.message
									BErr.Not_Signed_In -> Log.i("blah","not signed in or no internet") // redirect to login
									BErr.Exception -> Log.i("blah","exception") // other network issue
								} }
						}) { Text("get news") }
					}





				}
			}
		}

@Composable
fun QuestionnaireSelect(navController: NavController){
    val modifier = Modifier
    Column(modifier.fillMaxSize()) {
        Button(onClick = {navController.navigate(Screen.Settings.route)}) {
            Text("Lifestyle Questionnaire")
        }
        Spacer(modifier.height(16.dp))
        Button(onClick = {navController.navigate(Screen.QuestionnaireC.route)}) {
            Text("Cognitive Questionnaire")
        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen( modifier: Modifier = Modifier) {
    var testHttp: String by remember { mutableStateOf("")} // sample variable to show output to UI
    val ctx = LocalContext.current // need to pass "context" as argument when calling request
    var testIP by rememberSaveable { mutableStateOf(value="localhost")}
    var name = rememberSaveable { mutableStateOf("") }
    val Diabetic = rememberSaveable { mutableStateOf("") }
    var alcohol = rememberSaveable { mutableStateOf("") }
    var heartRate = rememberSaveable { mutableStateOf("") }
    var BloodOxygenLevel = rememberSaveable { mutableStateOf("")}
    var BodyTemperature = rememberSaveable { mutableStateOf("") }
    var Weight = rememberSaveable { mutableStateOf("") }
    var MRI_Delay = rememberSaveable { mutableStateOf("") }
    var prescription_name = rememberSaveable { mutableStateOf("") }
    var prescription_dosage = rememberSaveable { mutableStateOf("") }
    var Age = rememberSaveable { mutableStateOf("") }
    var educationLevel by rememberSaveable { mutableStateOf(EducationLevel.No) }
    var DominantHand = rememberSaveable { mutableStateOf("Right") }
    var gender = rememberSaveable { mutableStateOf("Male") }
    var FamilyHistory = rememberSaveable { mutableStateOf("Yes") }
    var smokingStatus by rememberSaveable { mutableStateOf(SmokingStatus.Never) }
    var APOEE4 = rememberSaveable { mutableStateOf("Yes") }
    var physicalActivity by rememberSaveable { mutableStateOf(PhysicalActivity.Mild) }
    var DepressionStatus = rememberSaveable { mutableStateOf("Yes") }
    var MedicationHistory = rememberSaveable { mutableStateOf("Yes") }
    var nutrientDiet by rememberSaveable { mutableStateOf(NutritionDiet.Balanced) }
    var sleepQuality  by rememberSaveable { mutableStateOf(SleepQuality.Poor) }
    var chronicHealthConditions by rememberSaveable { mutableStateOf(ChronicHealthConditions.None) }


    Box(modifier
        .fillMaxSize()
        .background(Color.White),
        contentAlignment = Alignment.Center) {
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
                text = "Lifestyle Questionnaire",
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
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = Diabetic.value == "Yes",
                        onClick = { Diabetic.value = "Yes" }
                    )
                    Text("Yes")
                }

                // Left option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = Diabetic.value == "No",
                        onClick = { Diabetic.value = "No" }
                    )
                    Text("No")
                }
            }


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
                value = prescription_name.value,
                onValueChange = { prescription_name.value = it },
                label = { Text("Enter your prescription name") },

                )
            TextField(
                value = prescription_dosage.value,
                onValueChange = { prescription_dosage.value = it },
                label = { Text("Enter your prescription dosage") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            TextField(
                value = Age.value,
                onValueChange = { Age.value = it },
                label = { Text("Enter your age") },

                )

            Spacer(modifier = Modifier.height(30.dp))

            var expanded_Educ by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded_Educ,
                onExpandedChange = { expanded_Educ = !expanded_Educ }
            ) {
                // The box/field that shows the user selection
                TextField(
                    value = educationLevel.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Education Level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded_Educ)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded_Educ,
                    onDismissRequest = { expanded_Educ = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No") },
                        onClick = { educationLevel = EducationLevel.No; expanded_Educ = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Primary") },
                        onClick = { educationLevel = EducationLevel.Primary; expanded_Educ = false   }
                    )
                    DropdownMenuItem(
                        text = { Text("Secondary") },
                        onClick = { educationLevel = EducationLevel.Secondary; expanded_Educ = false   }
                    )
                    DropdownMenuItem(
                        text = { Text("Diploma Degree") },
                        onClick = { educationLevel = EducationLevel.DeplomaDegree; expanded_Educ = false   }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Dominant Hand?")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = DominantHand.value == "Right",
                        onClick = { DominantHand.value = "Right" }
                    )
                    Text("Right")
                }

                // Left option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = DominantHand.value == "Left",
                        onClick = { DominantHand.value = "Left" }
                    )
                    Text("Left")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Gender?")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = gender.value == "Male",
                        onClick = { gender.value = "Male" }
                    )
                    Text("Male")
                }

                // Left option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = gender.value == "Female",
                        onClick = { gender.value = "Female" }
                    )
                    Text("Female")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Family History?")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = FamilyHistory.value == "Yes",
                        onClick = { FamilyHistory.value = "Yes" }
                    )
                    Text("Yes")
                }

                    // Left option
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = FamilyHistory.value == "No",
                            onClick = { FamilyHistory.value = "No" }
                        )
                        Text("No")
                    }
                }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Smoking")

            var expanded_Smoking by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded_Smoking,
                onExpandedChange = { expanded_Smoking = !expanded_Smoking }
            ) {
                // The box/field that shows the user selection
                TextField(
                    value = smokingStatus.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Smoking") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded_Smoking)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded_Smoking,
                    onDismissRequest = { expanded_Smoking = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Current") },
                        onClick = { smokingStatus = SmokingStatus.Current; expanded_Smoking = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Former") },
                        onClick = { smokingStatus = SmokingStatus.Former; expanded_Smoking = false   }
                    )
                    DropdownMenuItem(
                        text = { Text("Never") },
                        onClick = { smokingStatus = SmokingStatus.Never; expanded_Smoking = false   }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("APOEE4?")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = APOEE4.value == "Yes",
                        onClick = { APOEE4.value = "Yes" }
                    )
                    Text("Yes")
                }

                    // Left option
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = APOEE4.value == "No",
                            onClick = { APOEE4.value = "No" }
                        )
                        Text("No")
                    }
                }


            Spacer(modifier = Modifier.height(30.dp))

            Text("Physical Activity?")


            var expanded_PhyAct by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded_PhyAct,
                onExpandedChange = { expanded_PhyAct = !expanded_PhyAct }
            ) {
                // The box/field that shows the user selection
                TextField(
                    value = physicalActivity.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Physical Activity Level") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded_PhyAct)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded_PhyAct,
                    onDismissRequest = { expanded_PhyAct = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sedentary") },
                        onClick = { physicalActivity = PhysicalActivity.Sedentary; expanded_PhyAct = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Mild") },
                        onClick = { physicalActivity = PhysicalActivity.Mild; expanded_PhyAct = false  }
                    )
                    DropdownMenuItem(
                        text = { Text("Moderate") },
                        onClick = { physicalActivity = PhysicalActivity.Moderate; expanded_PhyAct = false  }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Depression Status?")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = DepressionStatus.value == "Yes",
                        onClick = { DepressionStatus.value = "Yes" }
                    )
                    Text("Yes")
                }
                    // Left option
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = DepressionStatus.value == "No",
                            onClick = { DepressionStatus.value = "No" }
                        )
                        Text("No")
                    }
                }


            Spacer(modifier = Modifier.height(30.dp))

            Text("Medication History?")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Right option
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = MedicationHistory.value == "Yes",
                        onClick = { MedicationHistory.value = "Yes" }
                    )
                    Text("Yes")
                }

                    // Left option
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = MedicationHistory.value == "No",
                            onClick = { MedicationHistory.value = "No" }
                        )
                        Text("No")
                    }
                }


            Spacer(modifier = Modifier.height(30.dp))

            Text("Nutrient Diet?")

            var expanded_Diet by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded_Diet,
                onExpandedChange = { expanded_Diet = !expanded_Diet }
            ) {
                // The box/field that shows the user selection
                TextField(
                    value = nutrientDiet.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nutrition Diet") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded_Diet)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded_Diet,
                    onDismissRequest = { expanded_Diet = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Balanced") },
                        onClick = { nutrientDiet = NutritionDiet.Balanced; expanded_Diet = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Mediterranean") },
                        onClick = { nutrientDiet = NutritionDiet.Mediterranean; expanded_Diet = false  }
                    )
                    DropdownMenuItem(
                        text = { Text("LowCarb") },
                        onClick = { nutrientDiet = NutritionDiet.LowCarb; expanded_Diet = false }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Sleep Quality?")

            var expanded_Sleep by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded_Sleep,
                onExpandedChange = { expanded_Sleep = !expanded_Sleep }
            ) {
                // The box/field that shows the user selection
                TextField(
                    value = sleepQuality.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sleep Quality") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded_Sleep)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded_Sleep,
                    onDismissRequest = { expanded_Sleep = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Poor") },
                        onClick = { sleepQuality = SleepQuality.Poor; expanded_Sleep = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Good") },
                        onClick = { sleepQuality = SleepQuality.Good; expanded_Sleep = false  }
                    )

                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text("Chronic Health Conditions?")


            var expanded_CHC by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded_CHC,
                onExpandedChange = { expanded_CHC = !expanded_CHC }
            ) {
                // The box/field that shows the user selection
                TextField(
                    value = chronicHealthConditions.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chronic Health Conditions?") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded_CHC)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded_CHC,
                    onDismissRequest = { expanded_CHC = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = { chronicHealthConditions = ChronicHealthConditions.None; expanded_CHC = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Hypertension") },
                        onClick = { chronicHealthConditions = ChronicHealthConditions.Hypertension; expanded_CHC = false  }
                    )
                    DropdownMenuItem(
                        text = { Text("Hearth Disease") },
                        onClick = { chronicHealthConditions = ChronicHealthConditions.HearthDisease; expanded_CHC = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Diabetes") },
                        onClick = { chronicHealthConditions = ChronicHealthConditions.Diabetes; expanded_CHC = false }
                    )
                }
            }


                // For backend testing, can remove if want
                Button(onClick = {
                    val patientData = LifestyleData(
                        Diabetic = if (Diabetic.value == "Yes") true else false,
                        AlcoholLevel = alcohol.value.toFloat(),
                        HeartRate = heartRate.value.toInt(),
                        BloodOxygenLevel = BloodOxygenLevel.value.toFloat(),
                        BodyTemperature = BodyTemperature.value.toFloat(),
                        Weight = Weight.value.toFloat(),
                        MRI_Delay = MRI_Delay.value.toFloat(),
                        Prescription = Prescription(prescription_name.value, prescription_dosage.value.toInt() ),
                        Age = Age.value.toInt(),
                        EducationLevel = educationLevel,
                        DominantHand = if (DominantHand.value == "Right") DomHand.Right else DomHand.Left,
                        Gender = if (gender.value == "Male") Gender.Male else Gender.Female,
                        FamilyHistory = if (FamilyHistory.value == "Yes") true else false,
                        SmokingStatus = smokingStatus,
                        APOEE4 = if (APOEE4.value == "Yes") true else false,
                        PhysicalActivity = physicalActivity,
                        DepressionStatus = if (DepressionStatus.value == "Yes") true else false,
                        MedicationHistory = if (MedicationHistory.value == "Yes") true else false,
                        NutrientDiet = nutrientDiet,
                        SleepQuality = sleepQuality,
                        ChronicHealthConditions = chronicHealthConditions
                    )
                    backend.send_lifestyle(
                        ctx,
                        patientData
                    ) { resp -> // anonymous function is called when backend responds
                        Log.i("CONOR",resp.toString())
                    }

                }) { Text("Submit") }
            }


        }
    }


@Composable
fun QuestionnaireScreenC(Cvm: MmseViewModel = MmseViewModel()){
    val state by Cvm.state.collectAsState()
    val score by remember(state) { mutableIntStateOf(Cvm.computeScore()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MMSE", style = MaterialTheme.typography.headlineSmall)
        Text("Included sections: Orientation, Registration, Attention/Calculation, Recall, Language (repetition).")
        AssistChip(onClick = {}, label = { Text("Score: $score / 22") })

        // ORIENTATION
        SectionCard("Orientation — Time (5)") {
            val labels = listOf("Year", "Season", "Date (day of month)", "Day", "Month")
            labels.forEachIndexed { i, label ->
                OutlinedTextField(
                    value = state.orientationTime[i],
                    onValueChange = { Cvm.setOrientationTime(i, it) },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SectionCard("Orientation — Place (5)") {
            val labels = listOf("Country", "County", "Town", "Hospital", "Floor")
            labels.forEachIndexed { i, label ->
                OutlinedTextField(
                    value = state.orientationPlace[i],
                    onValueChange = { Cvm.setOrientationPlace(i, it) },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SectionCard("Registration (3)") {

            Text("Examiner: Enter 3 words to be repeated")

            state.registrationWordsShown.forEachIndexed { i, _ ->
                OutlinedTextField(
                    value = state.registrationWordsShown[i],
                    onValueChange = { Cvm.setRegistrationWord(i, it) },
                    label = { Text("Word ${i + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Text("Patient: Immediate repetition")

            state.registrationImmediate.forEachIndexed { i, _ ->
                OutlinedTextField(
                    value = state.registrationImmediate[i],
                    onValueChange = { Cvm.setRegistrationImmediate(i, it) },
                    label = { Text("Response ${i + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ATTENTION & CALCULATION
        SectionCard("Attention and Calculation (5)") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.attentionMode == AttentionMode.SERIAL_7S,
                    onClick = { Cvm.setAttentionMode(AttentionMode.SERIAL_7S) },
                    label = { Text("Serial 7s") }
                )
                FilterChip(
                    selected = state.attentionMode == AttentionMode.WORLD_BACKWARD,
                    onClick = { Cvm.setAttentionMode(AttentionMode.WORLD_BACKWARD) },
                    label = { Text("WORLD backward") }
                )
            }

            Spacer(Modifier.height(8.dp))

            when (state.attentionMode) {
                AttentionMode.SERIAL_7S -> {
                    Text("Enter up to 5 answers (1 point each).")
                    state.serial7Answers.forEachIndexed { i, _ ->
                        OutlinedTextField(
                            value = state.serial7Answers[i],
                            onValueChange = { Cvm.setSerial7(i, it) },
                            label = { Text("Answer ${i + 1}") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                AttentionMode.WORLD_BACKWARD -> {
                    Text("Spell “world” backward.")
                    OutlinedTextField(
                        value = state.worldBackward,
                        onValueChange = Cvm::setWorldBackward,
                        label = { Text("Input") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // RECALL
        SectionCard("Recall (3)") {
            Text("Ask for the 3 objects from Registration.")
            state.recallAnswers.forEachIndexed { i, _ ->
                OutlinedTextField(
                    value = state.recallAnswers[i],
                    onValueChange = { Cvm.setRecall(i, it) },
                    label = { Text("Recalled object ${i + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // LANGUAGE (Repetition only)
        SectionCard("Language — Repetition (1)") {
            Text("Repeat the following:")
            Text("“No ifs, ands, or buts”", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.repetitionInput,
                onValueChange = Cvm::setRepetition,
                label = { Text("Patient response") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium)
                content()
            }
        )
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
fun LoginScreen( navController: NavController, uvm: UserViewModel = UserViewModel()) {
    val c = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        // Username Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Default.Visibility
                        else
                            Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password"
                        else "Show password"
                    )
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                backend.login(c, email,password, {x ->
                    when(x.err){
                        BErr.Ok -> {
                          if (x.success){
                              Log.i("blah","succ")
                              navController.navigate(Screen.Home.route)
                          } else { Log.i("blah","fail") }

                        }
                        BErr.Not_Signed_In -> Log.i("blah","internet fucky wucky") // no internet // cannot connect to server
                        BErr.Exception -> Log.i("blah","exception") // other network issue
                    }
                   })
                },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log In")
        }

        Spacer(Modifier.height(16.dp))

        // Sign Up link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Don't have an account? ")
            Text(
                text = "Sign Up",
                modifier = Modifier.clickable { navController.navigate(Screen.Signup.route) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var doctor by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val doctorList = listOf("Doctor 1", "Doctor 2", "Doctor 3", "Doctor 4")

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isValid = name.isNotBlank() &&
            Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            doctor.isNotBlank() &&
            password.length >= 6 &&
            password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Create Account")
        Spacer(Modifier.height(24.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(16.dp))

        // Doctor Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {

            OutlinedTextField(
                value = doctor,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Doctor") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                doctorList.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            doctor = item
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        // Sign Up Button
        Button(
            onClick = {navController.navigate(Screen.Login.route)},
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(Modifier.height(16.dp))

        // Navigate to login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
            TextButton(onClick = {navController.navigate(Screen.Login.route)}) {
                Text("Login")
            }
        }
    }
}



@Composable
fun Mainfunction(uvm: UserViewModel = UserViewModel(), Cvm: MmseViewModel =MmseViewModel()) {
    val navController = rememberNavController()
    val navItemList = listOf(
        NavItem(label = "Home", icon = Icons.Default.Home, screen = Screen.Home),
        NavItem(label = "Questionnaire", icon = Icons.Default.AccountBox, screen = Screen.QuestionnaireSelect),
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
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(uvm) }
            composable(Screen.Settings.route) { QuestionScreen() }
            composable(Screen.QuestionnaireC.route) { QuestionnaireScreenC(Cvm) }
            composable(Screen.QuestionnaireSelect.route) { QuestionnaireSelect(navController) }
            composable(Screen.Notification.route) { NotificationScreen() }
            composable(Screen.Login.route) { LoginScreen(navController, uvm) }
            composable(Screen.Signup.route) { SignUpScreen(navController) }
        }
    }
}
