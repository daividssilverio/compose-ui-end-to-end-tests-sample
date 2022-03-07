package com.example.testzeuitests

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.testzeuitests.ui.theme.TestZeUiTestsTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var isSyncingCallback: ((Boolean) -> Unit)? = null
    var currentRouteCallback: ((NavDestination?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestZeUiTestsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DummyApp(
                        currentRouteCallback = {
                            currentRouteCallback?.invoke(it)
                        }
                    )
                }
            }
        }

        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData(SlowSyncWorker.name)
            .observe(this) { workInfos ->
                runOnUiThread {
                    isSyncingCallback?.invoke(
                        workInfos.isNotEmpty() &&
                                workInfos.any { workInfo -> workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED }
                    )
                }
            }
    }
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val workManagerProvider: WorkManagerProvider,
    private val dao: DummyDao
) : ViewModel() {

    fun user(): Flow<User?> = dao.getUserFlow()

    fun data(): Flow<List<SomeData>> = dao.getAllDataFlow()

    suspend fun signIn(name: String) {
        withContext(Dispatchers.IO) {
            dao.addUser(User(name = name))

            val work = OneTimeWorkRequestBuilder<SlowSyncWorker>()
                .build()

            workManagerProvider.getWorkManager()
                .beginUniqueWork(
                    SlowSyncWorker.name,
                    ExistingWorkPolicy.REPLACE,
                    work
                ).enqueue()
        }
    }
}

@Composable
fun DummyApp(
    viewModel: AppViewModel = hiltViewModel(),
    currentRouteCallback: ((NavDestination?) -> Unit) = {}
) {
    val user by viewModel.user().collectAsState(initial = null)
    val data by viewModel.data().collectAsState(initial = emptyList())

    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    NavHost(navController = navController, "start") {
        composable("start") {
            Placeholder()
        }
        composable("login") {
            Login {
                coroutineScope.launch {
                    viewModel.signIn("cool user")
                }
            }
        }
        composable("sync") {
            Sync()
        }
        composable("list") {
            List(someData = data)
        }
    }

    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRouteCallback(destination)
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    LaunchedEffect(user, data) {
        if (user == null && data.isEmpty()) {
            currentRouteCallback(null)
            navController.navigate("login")
        } else if (user != null && data.isEmpty()) {
            currentRouteCallback(null)
            navController.navigate("sync")
        } else {
            currentRouteCallback(null)
            navController.navigate("list")
        }
    }
}

@Composable
fun Placeholder() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "Placeholder")
    }
}

@Composable
fun Login(onLogin: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { onLogin() }) {
            Text(text = "Login mate")
        }
    }
}

@Composable
fun Sync() {
    Box(Modifier.fillMaxSize()) {
        Text("syncing...", modifier = Modifier.testTag("sync"))
    }
}

@Composable
fun List(someData: List<SomeData>) {
    Box(modifier = Modifier.testTag("the list")) {
        Text(text = someData.first().value)
    }
}