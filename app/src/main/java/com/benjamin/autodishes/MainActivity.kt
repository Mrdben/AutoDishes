package com.benjamin.autodishes

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.benjamin.autodishes.ui.theme.AutoDishesTheme
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- 1. CLEAN DATA MODELS ---
data class MealRequest(
    val name: String,
    val diseases: String,
    val allergies: String,
    val preferences: String
)

data class MealResponse(
    val mealPlan: String
)

// --- 2. INTEGRATED SERVICE (Replaces MealApiService.kt) ---
interface AliCloudService {
    // Leave this as an empty string because the whole path is in the Base URL
    @POST("recommendMeals")
    suspend fun getMealPlan(@Body request: MealRequest): MealResponse
}

object RetrofitClient {
    // Use the EXACT prefix that worked in Postman
    private const val BASE_URL = "https://fc-mp-334e6ddc-7a19-4d9e-ae02-03449157fb4e-custom.next.bspapp.com/"

    val instance: AliCloudService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AliCloudService::class.java)
    }
}

// --- 4. MAIN ACTIVITY ---
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
                            navController.navigate("main") { popUpTo("setup") { inclusive = true } }
                        })
                    }
                    composable("main") {
                        MainAppContainer(
                            navController = navController,
                            onNavigateToAddProfile = { navController.navigate("add_profile/-1") },
                            onLogout = {
                                sharedPrefs.edit().clear().apply()
                                username = null
                                navController.navigate("setup") { popUpTo("main") { inclusive = true } }
                            }
                        )
                    }
                    composable(
                        route = "add_profile/{profileId}",
                        arguments = listOf(navArgument("profileId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val profileId = backStackEntry.arguments?.getInt("profileId") ?: -1
                        AddProfileScreen(profileId = profileId, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

// --- 5. SCREEN CONTENT ---
@Composable
fun HomeScreenContent(navController: androidx.navigation.NavHostController, onNavigateToAddProfile: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    var profiles by remember { mutableStateOf(listOf<ElderProfile>()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val markwon = remember { Markwon.create(context) }

    var aiResult by remember { mutableStateOf<String?>(null) }
    var isAiGenerating by remember { mutableStateOf(false) }

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
            Text("No profiles stored locally.", color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onEdit = { navController.navigate("add_profile/${profile.id}") },
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
            onClick = {
                if (profiles.isNotEmpty()) {
                    isAiGenerating = true
                    val target = profiles.first()
                    scope.launch {
                        try {
                            // HERE IS THE CORE CALL
                            val request = MealRequest(target.name, target.diseases, target.allergies, target.preferences)
                            val response = RetrofitClient.instance.getMealPlan(request)
                            aiResult = response.mealPlan
                        } catch (e: Exception) {
                            // This will now show the REAL reason for 405 if it persists
                            Toast.makeText(context, "Sync Failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isAiGenerating = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Add a profile first", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            enabled = !isAiGenerating
        ) {
            if (isAiGenerating) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Generate Meal Plan (Qwen-Max)")
        }
    }

    if (aiResult != null) {
        AlertDialog(
            onDismissRequest = { aiResult = null },
            title = { Text("Recommended Meal Plan") },
            text = {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp).verticalScroll(rememberScrollState()),
                    factory = { ctx -> android.widget.TextView(ctx).apply {
                        textSize = 16f
                        setTextColor(android.graphics.Color.BLACK)
                    } },
                    update = { textView -> markwon.setMarkdown(textView, aiResult!!) }
                )
            },
            confirmButton = { Button(onClick = { aiResult = null }) { Text("Close") } }
        )
    }
}

// ... Ensure the rest of your UI (SetupScreen, AddProfileScreen, etc.)
// remains below this in the same file or package ...
// --- SUPPORTING UI ---

@Composable
fun SetupScreen(onSetupComplete: (String) -> Unit) {
    var nameInput by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Welcome to AutoDishes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Your Name") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
            Text("I accept the terms", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { onSetupComplete(nameInput) }, enabled = nameInput.isNotBlank() && termsAccepted, modifier = Modifier.fillMaxWidth()) { Text("Enter") }
    }
}

@Composable
fun MainAppContainer(navController: androidx.navigation.NavHostController, onNavigateToAddProfile: () -> Unit, onLogout: () -> Unit) {
    var selectedItem by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Home") }, selected = selectedItem == 0, onClick = { selectedItem = 0 })
                NavigationBarItem(icon = { Icon(Icons.Filled.Settings, null) }, label = { Text("Settings") }, selected = selectedItem == 1, onClick = { selectedItem = 1 })
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (selectedItem == 0) HomeScreenContent(navController, onNavigateToAddProfile)
            else SettingsScreenContent(onLogout)
        }
    }
}

@Composable
fun ProfileCard(profile: ElderProfile, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(profile.name, style = MaterialTheme.typography.titleLarge)
            Text("Conditions: ${profile.diseases}")
            Text("Allergies: ${profile.allergies}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun SettingsScreenContent(onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(40.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
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
    var name by remember { mutableStateOf("") }
    var diseases by remember { mutableStateOf(setOf<String>()) }
    var allergies by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }

    LaunchedEffect(profileId) {
        if (profileId != -1) {
            val p = db.profileDao().getAllProfiles().find { it.id == profileId }
            p?.let {
                name = it.name
                allergies = it.allergies
                preferences = it.preferences
                diseases = it.diseases.split(", ").filter { it.isNotBlank() }.toSet()
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Profile Setup") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { p ->
        Column(modifier = Modifier.padding(p).padding(24.dp)) {
            when(currentStep) {
                1 -> OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                2 -> listOf("Diabetes", "Hypertension", "None").forEach { d ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = diseases.contains(d), onCheckedChange = { if(it) diseases += d else diseases -= d })
                        Text(d)
                    }
                }
                3 -> OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Allergies") }, modifier = Modifier.fillMaxWidth())
                4 -> OutlinedTextField(value = preferences, onValueChange = { preferences = it }, label = { Text("Preferences") }, modifier = Modifier.fillMaxWidth())
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                if (currentStep < 4) currentStep++
                else scope.launch {
                    val profile = ElderProfile(id = if(profileId == -1) 0 else profileId, name = name, diseases = diseases.joinToString(", "), allergies = allergies, preferences = preferences)
                    if(profileId == -1) db.profileDao().insertProfile(profile) else db.profileDao().updateProfile(profile)
                    onBack()
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank()) { Text(if(currentStep < 4) "Next" else "Save") }
        }
    }
}