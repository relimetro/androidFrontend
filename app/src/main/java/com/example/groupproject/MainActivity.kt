package com.example.groupproject

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
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
import java.security.KeyStore


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
    val orientationPlace: List<String> = List(5) { "" },  // country, county, town, hospital, floor

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

    @RequiresApi(Build.VERSION_CODES.O)
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
    private val _latestResult = MutableStateFlow<MmseResult?>(null)
    val latestResult: StateFlow<MmseResult?> = _latestResult.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMmseResult() {
        val result = MmseResult(
            score = computeScore(),
            takenAt = LocalDateTime.now()
        )
        _latestResult.value = result
    }
}



class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroupProjectTheme {
                val uvm: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                val mmseVm: MmseViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                Mainfunction(uvm, mmseVm)
            }
        }
    }
}

data class MmseResult(
    val score: Int,
    val takenAt: LocalDateTime
)

data class RiskResult(
    val score: String,
    val calculatedAt: LocalDateTime
)

class UserViewModel: ViewModel() {
    private val _latestRisk = MutableStateFlow<RiskResult?>(null)
    val latestRisk: StateFlow<RiskResult?> = _latestRisk.asStateFlow()
    var _username = MutableStateFlow("Conor") // private, screens read using indes (not _index) and write using provided methods
    var username: StateFlow<String> = _username.asStateFlow()
    var isLoggedIn by mutableStateOf(true)
            private set
    fun onLoginSuccess() {
        isLoggedIn = true
    }
    fun logout() {
        isLoggedIn = false
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveRiskScore(score: String) {
        _latestRisk.value = RiskResult(
            score = score,
            calculatedAt = LocalDateTime.now()
        )
    }
    fun onLoginSuccess(name: String) {
        _username.value = name
        isLoggedIn = true
    }

}

@Composable
fun PageBanner(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 24.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(uvm: UserViewModel) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            PageBanner(title = "Home")
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val username by uvm.username.collectAsState()
                SectionCard(title = "") {
                    Text(
                        text = "Welcome ${if (username.isNotBlank()) username else "User"}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Here is your health overview",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                SectionCard(title = "Risk Score") {
                    val ctx = LocalContext.current
                    val latestRisk by uvm.latestRisk.collectAsState()


                    if (latestRisk == null) {
                        Text("No risk score calculated yet.")
                    } else {
                        Text(
                            text = latestRisk!!.score,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Calculated on: ${
                                latestRisk!!.calculatedAt.format(
                                    java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                                )
                            }"
                        )
                    }

                    Button(
                        onClick = {
                            backend.request_risk(ctx) { resp ->
                                when (resp.err) {
                                    BErr.Ok -> uvm.saveRiskScore(resp.message)
                                    else -> {  }
                                }
                            }
                        }
                    ) {
                        Text("Calculate Risk")
                    }
                }


                SectionCard(title = "Health News") {
                    var myNews by rememberSaveable { mutableStateOf("No news loaded") }
                    val ctx = LocalContext.current

                    Text(
                        text = myNews,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            backend.request_news(ctx) { resp ->
                                when (resp.err) {
                                    BErr.Ok -> myNews = resp.message
                                    else -> myNews = "Unable to load news"
                                }
                            }
                        }
                    ) {
                        Text("Get News")
                    }
                }
            }
        }
    }
}



@Composable
fun QuestionnaireSelect(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        PageBanner(title = "Questionnaire Select")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.Settings.route)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Lifestyle Questionnaire",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "General health and lifestyle questionnaire",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.QuestionnaireC.route)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Mini Mental State Exam",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cognitive screening assessment",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionScreen( modifier: Modifier = Modifier) {
    var testHttp: String by remember { mutableStateOf("") } // sample variable to show output to UI
    val ctx = LocalContext.current // need to pass "context" as argument when calling request
    var testIP by rememberSaveable { mutableStateOf(value = "localhost") }
    var name = rememberSaveable { mutableStateOf("") }
    val Diabetic = rememberSaveable { mutableStateOf("") }
    var alcohol = rememberSaveable { mutableStateOf("") }
    var heartRate = rememberSaveable { mutableStateOf("") }
    var BloodOxygenLevel = rememberSaveable { mutableStateOf("") }
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
    var sleepQuality by rememberSaveable { mutableStateOf(SleepQuality.Poor) }
    var chronicHealthConditions by rememberSaveable { mutableStateOf(ChronicHealthConditions.None) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageBanner(title = "Lifestyle Questionnaire")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SectionCard(title = "Personal Information") {

                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = Age.value,
                    onValueChange = { Age.value = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gender")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

            SectionCard(title = "Medical History") {

                Text("Diabetic")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                }

                Text("Family History")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                }
            }

            SectionCard(title = "Lifestyle") {

                Text("Smoking Status")
                var expanded_Smoking by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded_Smoking,
                    onExpandedChange = { expanded_Smoking = !expanded_Smoking }
                ) {

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
                            onClick = {
                                smokingStatus = SmokingStatus.Current; expanded_Smoking = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Former") },
                            onClick = {
                                smokingStatus = SmokingStatus.Former; expanded_Smoking = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Never") },
                            onClick = {
                                smokingStatus = SmokingStatus.Never; expanded_Smoking = false
                            }
                        )
                    }
                }

                Text("Physical Activity")
                var expanded_PhyAct by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded_PhyAct,
                    onExpandedChange = { expanded_PhyAct = !expanded_PhyAct }
                ) {

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
                            onClick = {
                                physicalActivity = PhysicalActivity.Sedentary; expanded_PhyAct =
                                false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mild") },
                            onClick = {
                                physicalActivity = PhysicalActivity.Mild; expanded_PhyAct = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Moderate") },
                            onClick = {
                                physicalActivity = PhysicalActivity.Moderate; expanded_PhyAct =
                                false
                            }
                        )
                    }
                }

                Text("Nutrition")
                var expanded_Diet by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded_Diet,
                    onExpandedChange = { expanded_Diet = !expanded_Diet }
                ) {

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
                            onClick = {
                                nutrientDiet = NutritionDiet.Balanced; expanded_Diet = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mediterranean") },
                            onClick = {
                                nutrientDiet = NutritionDiet.Mediterranean; expanded_Diet = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("LowCarb") },
                            onClick = {
                                nutrientDiet = NutritionDiet.LowCarb; expanded_Diet = false
                            }
                        )
                    }
                }

                Text("Sleep Quality")
                var expanded_Sleep by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded_Sleep,
                    onExpandedChange = { expanded_Sleep = !expanded_Sleep }
                ) {
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
                            onClick = { sleepQuality = SleepQuality.Good; expanded_Sleep = false }
                        )

                    }
                }
            }

            SectionCard(title = "Vital Signs") {

                OutlinedTextField(
                    value = heartRate.value,
                    onValueChange = { heartRate.value = it },
                    label = { Text("Heart Rate (bpm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = BloodOxygenLevel.value,
                    onValueChange = { BloodOxygenLevel.value = it },
                    label = { Text("Blood Oxygen (%)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = BodyTemperature.value,
                    onValueChange = { BodyTemperature.value = it },
                    label = { Text("Body Temperature (°C)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = Weight.value,
                    onValueChange = { Weight.value = it },
                    label = { Text("Weight (kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SectionCard(title = "Submit") {
                Button(
                    onClick = {
                        val patientData = LifestyleData(
                            Diabetic = if (Diabetic.value == "Yes") true else false,
                            AlcoholLevel = alcohol.value.toFloat(),
                            HeartRate = heartRate.value.toInt(),
                            BloodOxygenLevel = BloodOxygenLevel.value.toFloat(),
                            BodyTemperature = BodyTemperature.value.toFloat(),
                            Weight = Weight.value.toFloat(),
                            MRI_Delay = MRI_Delay.value.toFloat(),
                            Prescription = Prescription(
                                prescription_name.value,
                                prescription_dosage.value.toInt()
                            ),
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
                            Log.i("CONOR", resp.toString())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Lifestyle Questionnaire")
                }
            }

        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuestionnaireScreenC(Cvm: MmseViewModel){
    val state by Cvm.state.collectAsState()
    val score by remember(state) { mutableIntStateOf(Cvm.computeScore()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PageBanner(title = "Mini Mental State Exam")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            AssistChip(
                onClick = {},
                label = { Text("Score: $score / 22") }
            )

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
            SectionCard(title = "Finish Assessment") {
                Button(
                    onClick = { Cvm.saveMmseResult() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save MMSE Result")
                }
            }

        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationScreen(Cvm: MmseViewModel, uvm: UserViewModel) {
    val modifier = Modifier
    val latestMmse by Cvm.latestResult.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        PageBanner(title = "Notifications")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val latestRisk by uvm.latestRisk.collectAsState()

            SectionCard(title = "Latest Risk Score") {
                if (latestRisk == null) {
                    Text("No risk score available yet.")
                } else {
                    Text(
                        text = latestRisk!!.score,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calculated on: ${
                            latestRisk!!.calculatedAt.format(
                                java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
                            )
                        }"
                    )
                }
            }
            SectionCard(title = "Latest MMSE") {
                if (latestMmse == null) {
                    Text("No MMSE recorded yet")
                } else {
                    Text(
                        text = "Score: ${latestMmse!!.score} / 22",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Taken on: ${
                            latestMmse!!.takenAt
                                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
                        }"
                    )
                }
            }
            SectionCard(title = "Information") {
                Text(
                    text = "This is where notifications about your diagnosis or assessments will appear.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    navController: NavController,
    uvm: UserViewModel
) {
    val c = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        PageBanner(title = "Login")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            SectionCard(title = "Sign In") {

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector =
                                    if (passwordVisible)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        backend.login(c, email, password) { x ->
                            when (x.err) {
                                BErr.Ok -> {
                                    if (x.success) {
                                        uvm.onLoginSuccess(email) // or username
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                                else -> {
                                    // Optional: show error message
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log In")
                }
            }

            SectionCard(title = "Create Account") {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account? ")
                    Text(
                        text = "Sign Up",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Signup.route)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var cont = LocalContext.current
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
            .background(MaterialTheme.colorScheme.background)
    ) {


        PageBanner(title = "Create Account")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            SectionCard(title = "Account Details") {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

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

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    visualTransformation =
                        if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

        Spacer(Modifier.height(24.dp))

            SectionCard(title = "Actions") {
                Button(
                    onClick = {
                        backend.signUp(cont, email, password) { x ->
                            when (x.err) {
                                BErr.Ok -> {
                                    backend.initUser(cont)
                                    backend.addUserDetail(cont, "Name",name)
                                    backend.addUserDetail(cont, "Email",email)
                                    backend.addUserDetail(cont, "testPassword",password)
                                    navController.navigate(Screen.Login.route)
                                }
                                BErr.Not_Signed_In -> Toast.makeText( cont, "Cannot Connect To Backend", Toast.LENGTH_SHORT ).show()
                                BErr.Exception -> Toast.makeText( cont, "Exception, see logs", Toast.LENGTH_SHORT ).show()
                            } }
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already have an account? ")
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }
            }
        }
    }
}




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Mainfunction(uvm: UserViewModel, Cvm: MmseViewModel) {
    val navController = rememberNavController()
    val isLoggedIn = uvm.isLoggedIn
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
            if (isLoggedIn) {
                BottomAppBar {
                    NavigationBar {
                        navItemList.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedIndex == index,
                                onClick = {
                                    selectedIndex = index
                                    if (navController.currentDestination?.route != item.screen.route) {
                                        navController.navigate(item.screen.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(text = item.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(uvm) }
            composable(Screen.Settings.route) { QuestionScreen() }
            composable(Screen.QuestionnaireC.route) { QuestionnaireScreenC(Cvm) }
            composable(Screen.QuestionnaireSelect.route) { QuestionnaireSelect(navController) }
            composable(Screen.Notification.route) { NotificationScreen(Cvm, uvm) }
            composable(Screen.Login.route) { LoginScreen(navController, uvm) }
            composable(Screen.Signup.route) { SignUpScreen(navController) }
        }
    }
}
