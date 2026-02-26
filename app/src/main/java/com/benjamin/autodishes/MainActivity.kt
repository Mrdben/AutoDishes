package com.benjamin.autodishes

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.benjamin.autodishes.ui.theme.AutoDishesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoDishesTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                val sharedPrefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
                var username by remember { mutableStateOf(sharedPrefs.getString("username", null)) }

                val startDestination = if (username != null) "main" else "setup"

                NavHost(navController = navController, startDestination = startDestination) {

                    composable("setup") {
                        SetupScreen(onSetupComplete = { newName ->
                            sharedPrefs.edit().putString("username", newName).apply()
                            username = newName
                            navController.navigate("main") {
                                popUpTo("setup") { inclusive = true }
                            }
                        })
                    }

                    composable("main") {
                        MainAppContainer(
                            navController = navController,
                            onNavigateToAddProfile = { navController.navigate("add_profile/-1") }, // -1 means new
                            onLogout = {
                                sharedPrefs.edit().clear().apply()
                                username = null
                                navController.navigate("setup") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Modified route to accept a profileId for editing
                    composable(
                        route = "add_profile/{profileId}",
                        arguments = listOf(navArgument("profileId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val profileId = backStackEntry.arguments?.getInt("profileId") ?: -1
                        AddProfileScreen(
                            profileId = profileId,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun SetupScreen(onSetupComplete: (String) -> Unit) {
    var nameInput by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome to AutoDishes", style = MaterialTheme.typography.headlineMedium)
        Text("First, let's set up your local account.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
            Text("I agree to the Terms of Service (Local Use Only)", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSetupComplete(nameInput) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = nameInput.isNotBlank() && termsAccepted
        ) {
            Text("Enter App")
        }
    }
}

@Composable
fun MainAppContainer(navController: androidx.navigation.NavHostController, onNavigateToAddProfile: () -> Unit, onLogout: () -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (selectedItem == 0) HomeScreenContent(navController = navController, onNavigateToAddProfile = onNavigateToAddProfile)
            else SettingsScreenContent(onLogout)
        }
    }
}

@Composable
fun HomeScreenContent(navController: androidx.navigation.NavHostController, onNavigateToAddProfile: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var profiles by remember { mutableStateOf(listOf<ElderProfile>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        profiles = db.profileDao().getAllProfiles()
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Family Profiles", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (profiles.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No profiles stored on this device.", color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Add one to start planning meals.", style = MaterialTheme.typography.bodySmall)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onEdit = {
                            navController.navigate("add_profile/${profile.id}")
                        },
                        onDelete = {
                            scope.launch {
                                db.profileDao().deleteProfile(profile)
                                profiles = db.profileDao().getAllProfiles()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToAddProfile, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add New Profile")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* Recommendation logic later */ },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Generate Meal Plan (AI)")
        }
    }
}

@Composable
fun ProfileCard(profile: ElderProfile, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Text(profile.name.take(1).uppercase())
                }
                Spacer(Modifier.width(12.dp))
                Text(text = profile.name, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = "Conditions: ${profile.diseases}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Allergies: ${profile.allergies}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val currentUsername = sharedPrefs.getString("username", "User")

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Logged in as:", style = MaterialTheme.typography.labelLarge)
                    Text(currentUsername ?: "Unknown", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("Switch User / Reset")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProfileScreen(profileId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(1) }
    var profileName by remember { mutableStateOf("") }
    var selectedDiseases by remember { mutableStateOf(setOf<String>()) }
    var allergies by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }

    // Load existing profile if editing
    LaunchedEffect(profileId) {
        if (profileId != -1) {
            val existing = db.profileDao().getAllProfiles().find { it.id == profileId }
            existing?.let {
                profileName = it.name
                allergies = it.allergies
                preferences = it.preferences
                selectedDiseases = it.diseases.split(", ").filter { d -> d.isNotBlank() }.toSet()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileId == -1) "Step $currentStep of 4" else "Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) currentStep-- else onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            when (currentStep) {
                1 -> {
                    Text("What is the elder's name?", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = profileName, onValueChange = { profileName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                }
                2 -> {
                    Text("Health Conditions", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    listOf("Diabetes", "Hypertension", "High Cholesterol", "None").forEach { disease ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = selectedDiseases.contains(disease),
                                onCheckedChange = {
                                    selectedDiseases = if (selectedDiseases.contains(disease)) {
                                        selectedDiseases - disease
                                    } else {
                                        if (disease == "None") setOf("None") else selectedDiseases - "None" + disease
                                    }
                                }
                            )
                            Text(disease)
                        }
                    }
                }
                3 -> {
                    Text("Any Allergies?", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("e.g. Peanuts, Seafood") }, modifier = Modifier.fillMaxWidth())
                }
                4 -> {
                    Text("Food Preferences", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = preferences, onValueChange = { preferences = it }, label = { Text("e.g. Vegetarian, Soft food") }, modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = (currentStep != 1 || profileName.isNotBlank()),
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        scope.launch {
                            val profile = ElderProfile(
                                id = if (profileId == -1) 0 else profileId, // 0 for auto-gen
                                name = profileName,
                                diseases = if(selectedDiseases.isEmpty()) "None" else selectedDiseases.joinToString(", "),
                                allergies = if(allergies.isBlank()) "None" else allergies,
                                preferences = if(preferences.isBlank()) "None" else preferences
                            )
                            if (profileId == -1) {
                                db.profileDao().insertProfile(profile)
                            } else {
                                db.profileDao().updateProfile(profile)
                            }
                            onBack()
                        }
                    }
                }
            ) {
                Text(if (currentStep < 4) "Next" else "Save Changes")
            }
        }
    }
}