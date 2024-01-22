package com.example.basicscodelab

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.basicscodelab.data.ListViewModel
import com.example.basicscodelab.presentation.SettingsDialog
import com.example.basicscodelab.presentation.ShareDialog
import com.example.basicscodelab.presentation.SignInScreen
import com.example.basicscodelab.sign_in.SignInViewModel
import com.example.basicscodelab.sign_in.UserData
import com.example.basicscodelab.sign_in.auth.GoogleAuthClient
import com.example.basicscodelab.ui.theme.BasicsCodelabTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val authentication by lazy {
        GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext),
            listViewModel = ListViewModel()
        )
    }
    private val listViewModel: ListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BasicsCodelabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val signInViewModel = viewModel<SignInViewModel>()
                            val state by signInViewModel.state.collectAsStateWithLifecycle()

                            // Check if the user is logged in and navigate in that case
                            LaunchedEffect(key1 = Unit) {
                                if(authentication.getSignedInUser() != null) {
                                    navController.navigate("main")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if(result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = authentication.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            signInViewModel.onSignInResult(signInResult)
                                        }
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "Sign in failed",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if(state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()
//                                    val intent = Intent(this@MainActivity, ListActivity::class.java)
//                                    startActivity(intent)

                                    // TODO direct the user to their list screen
                                    navController.navigate("lists_screen")
//                                    signInViewModel.resetState()
                                }
                            }
                            // TODO Implement the sign in screen composable here.
                            SignInScreen(
                                state,
                                onGoogleSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = authentication.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                })
                        }
                        composable("main") {
                            val signedInUser = authentication.getSignedInUser()
                            signedInUser?.let {
                                ScaffoldUI(listViewModel, navController,
                                    signOutCallback = {
                                        lifecycleScope.launch {
                                            authentication.signOut()
                                        }
                                        navController.navigate("sign_in")
                                    },
                                    signedInUser
                                )
                            }
                        }
                        // TODO direct the user to their list screen if they are signed in
                        composable("lists_screen") {
                            val signedInUser = authentication.getSignedInUser()
//                            if (signedInUser != null) {
//                                Log.d("Signed in userid: ", signedInUser.userId)
//                                signedInUser.userName?.let { it1 ->
//                                    Log.d("Signed in username: ",
//                                        it1
//                                    )
//                                }
//                            }
                            // TODO Use the signedInUser to get the lists of that specific user
                            if (signedInUser != null) {
                                DisplayAllListsScreen(listViewModel, navController, signedInUser,
                                    signOutCallback = {
                                        lifecycleScope.launch() {
                                            authentication.signOut()
                                        }
                                        navController.navigate("sign_in")
                                    })
                            }
//                            DisplayAllListsScreen(listViewModel, navController)

//                            ScaffoldUI(listViewModel, navController)
                        }
                        // TODO navigate to the specific item screen
                        composable("add_item_screen/{listName}") { backStackEntry ->
                            val listName = backStackEntry.arguments?.getString("listName")
                            val signedInUser = authentication.getSignedInUser()
                            if (listName != null) {
                                if (signedInUser != null) {
                                    AddItemScreen(listName, listViewModel, navController,
                                        signOutCallback = {
                                            lifecycleScope.launch {
                                                authentication.signOut()
                                            }
                                            navController.navigate("sign_in")
                                        },
                                        signedInUser)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
// TODO make this composable more modular so i can reuse it throughout my project
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldUI(
    listViewModel: ListViewModel,
    navController: NavController,
    signOutCallback: () -> Unit,
    user: UserData
) {
    var isSettingsDialogVisible by remember { mutableStateOf(false)}
    val showDialog = remember { mutableStateOf(false) }
    var listName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.padding(bottom = 40.dp),
        topBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopAppBar(
                    title = { Text("Welcome") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Add your settings button here
                        IconButton(
                            modifier = Modifier.size(66.dp),
                            onClick = { isSettingsDialogVisible = true }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .size(70.dp)
                    .fillMaxSize(),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = (-2).dp
                ),
                onClick = {
                    showDialog.value = true
                }
            ) { Text("New List") }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { navController.navigate("lists_screen") }
                ) {
                    Text(text = "View Your Lists")
                }
            }
        }
    }
    // Dialog for entering the list name
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog
                showDialog.value = false
            },
            title = {
                Text("Enter List Name")
            },
            text = {
                // TextField for entering the list name
                TextField(
                    value = listName,
                    onValueChange = {
                        listName = it
                    },
                    label = { Text("List Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Create the list and dismiss the dialog
                        listViewModel.createNewList(listName, user)
                        showDialog.value = false
                        navController.navigate("add_item_screen/${listName}")
                        listName = ""
                    },
                    enabled = listName.isNotEmpty()
                ) {
                    Text("Create List")
                }
            }
        )
    }
    if (isSettingsDialogVisible) {
        SettingsDialog(
            onSignOutClick = {
                // Call the signOut function from your GoogleAuthClient
                signOutCallback()
                isSettingsDialogVisible = false // Dismiss the dialog after sign out
            },
            onDismiss = { isSettingsDialogVisible = false }
        )
    }
}

// TODO need to display all the items in the list.
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    listName: String,
    listViewModel: ListViewModel,
    navController: NavController,
    signOutCallback: () -> Unit,
    user: UserData
) {
    var isSettingsDialogVisible by remember { mutableStateOf(false)}
    var shareDialogVisible by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val lifecycleOwner = LocalLifecycleOwner.current

    var itemName by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val listItems by listViewModel.listData.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        listViewModel.getListItems(listName, user)
    }
    Scaffold(
        modifier = Modifier.padding(bottom = 40.dp),
        topBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopAppBar(
                    title = { Text(listName) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Add your settings button here
                        IconButton(
                            modifier = Modifier.size(66.dp),
                            onClick = { isSettingsDialogVisible = true }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { shareDialogVisible = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Share List")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .size(70.dp)
                    .fillMaxSize(),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = (-2).dp
                ),
                onClick = {
                    showDialog.value = true
                }
            ) { Text("Add Item") }
        }
    ) {
        // Display all items in the list
        LazyColumn(
            modifier = Modifier
                .padding(top = 80.dp),

        ) {
            items(items = listItems) { listItem ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 4.dp)
                ) {
                    listItem.isChecked?.let {
                        Checkbox(
                            checked = it,
                            onCheckedChange = {
                                listViewModel
                                    .updateItemCheckedStatus(
                                        listName, listItem, user
                                    )
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    listItem.itemName?.let { Text(text = it) }
                }
            }
        }
    }
    if (shareDialogVisible) {
        ShareDialog(
            onDismiss = { shareDialogVisible = false },
            onShare = { sharedUserName ->
                // Perform the share operation with the entered username
                // You can call the shareList function from your ListViewModel
                // and pass the required parameters.
                lifecycleOwner.lifecycleScope.launch {
                    // Replace the following with your actual logic
                    listViewModel.shareList(listName, sharedUserName, user)
                    shareDialogVisible = false
                }
            }
        )
    }
    // Dialog for entering the list name
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog
                showDialog.value = false
            },
            title = {
                Text("Enter Item")
            },
            text = {
                // TextField for entering the list name
                TextField(
                    value = itemName,
                    onValueChange = {
                        itemName = it
                    },
                    label = { Text("Item Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        listViewModel.addItemToList(
                            listName,
                            itemName,
                            user
                        )
                        showDialog.value = false
                        itemName = ""

                    },
                    enabled = listName.isNotEmpty()
                ) {
                    Text("Add Item")
                }
            }
        )
    }
    if (isSettingsDialogVisible) {
        SettingsDialog(
            onSignOutClick = {
                // Call the signOut function from your GoogleAuthClient
                signOutCallback()
                isSettingsDialogVisible = false // Dismiss the dialog after sign out
            },
            onDismiss = { isSettingsDialogVisible = false }
        )
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAllListsScreen(
    listViewModel: ListViewModel,
    navController: NavController,
    signedInUser: UserData,
    signOutCallback: () -> Unit
) {
    var listName by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    var isSettingsDialogVisible by remember { mutableStateOf(false) }
    // Making sure UI updates when the data changes in database
    val listItems by listViewModel.listData.collectAsStateWithLifecycle()

    // Observe changes in listData and recompose when it changes
    LaunchedEffect(listItems) {
        listViewModel.loadListData(signedInUser)
    }
    Scaffold(
        modifier = Modifier
            .padding(bottom = 40.dp)
            .fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopAppBar(
                    title = { Text("${signedInUser.userName} this is your lists") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Add your settings button here
                        IconButton(
                            modifier = Modifier.size(66.dp),
                            onClick = { isSettingsDialogVisible = true }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .size(70.dp)
                    .fillMaxSize(),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = (-2).dp
                ),
                onClick = {
                    showDialog.value = true
                }
            ) { Text("New List") }
        }
    ) {
        // Display the lists in a grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(top = 70.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                .fillMaxHeight(),
        ) {
            if (listItems.isNotEmpty()) {
                // Display the lists here
                items(items = listItems) { listItem ->
//                    Button(
//                        onClick = {
//                            navController.navigate("add_item_screen/${listItem.itemName}")
//                        },
//                        modifier = Modifier
//                            .padding(8.dp)
//                            .size(30.dp)
//                            .background(color = MaterialTheme.colorScheme.primary)
//                            .size(10.dp),
//                        shape = CircleShape,
//                        elevation = ButtonDefaults.buttonElevation(
//                            defaultElevation = 2.dp,
//                            pressedElevation = (-2).dp
//                        )
//                    ) {
//                        Text(
//                            text = listItem.itemName ?: "",
//                            color = MaterialTheme.colorScheme.onPrimary
//                        )
//                    }
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(30.dp)
                            .background(color = MaterialTheme.colorScheme.primary)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate("add_item_screen/${listItem.itemName}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = listItem.itemName ?: "",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                // TODO need to handle no lists better!!
                // Handle the case where there are no lists
                item {
                    Text(text = "No lists available")
                }
            }
        }
    }
//    Box {
//        Text(text = "${signedInUser.userName}")
//    }
    // Dialog for entering the list name
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog
                showDialog.value = false
            },
            title = {
                Text("Enter List Name")
            },
            text = {
                // TextField for entering the list name
                TextField(
                    value = listName,
                    onValueChange = {
                        listName = it
                    },
                    label = { Text("List Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Create the list and dismiss the dialog
                        listViewModel.createNewList(listName, signedInUser)
                        showDialog.value = false
                        navController.navigate("add_item_screen/${listName}")
                        listName = ""
                    },
                    enabled = listName.isNotEmpty()
                ) {
                    Text("Create List")
                }
            }
        )
    }
    if (isSettingsDialogVisible) {
        SettingsDialog(
            onSignOutClick = {
                // Call the signOut function from your GoogleAuthClient
                signOutCallback()
                isSettingsDialogVisible = false // Dismiss the dialog after sign out
            },
            onDismiss = { isSettingsDialogVisible = false }
        )
    }
}