package com.example.basicscodelab

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.basicscodelab.ui.theme.BasicsCodelabTheme


class MainActivity : ComponentActivity() {
    private val listViewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicsCodelabTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "main") {
                        composable(route = "main") {
                            ScaffoldUI(listViewModel, navController)
                        }
                        composable(route = "add_item_screen/{listName}") {backStackEntry ->
                            val arguments = requireNotNull(backStackEntry.arguments)
                            val listName = arguments.getString("listName") ?: error("List name not provided")
                            AddItemScreen(listName, listViewModel, navController)
                        }
                        composable(route = "display_all_lists_screen") {
                            DisplayAllListsScreen(listViewModel, navController)
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
    navController: NavController
) {
    val showDialog = remember { mutableStateOf(false) }
    var listName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.padding(bottom = 40.dp),
        topBar = {
            Row (
                modifier = Modifier
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                TopAppBar(
                    title = { Text("Welcome") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Add your settings button here
                        IconButton(
                            modifier = Modifier.size(66.dp),
                            onClick = { /* Handle settings button click */ }
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
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp)) {
            Column {
                Button(onClick = { navController.navigate("display_all_lists_screen") }) {
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
                            listViewModel.createNewList(listName)
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
}

// TODO need to display all the items in the list.
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    listName: String,
    listViewModel: ListViewModel,
    navController: NavController
) {
    var itemName by remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

//    // Collect the result of getListItems into listItems state
//    val listItems by rememberUpdatedState(
//        newValue = listViewModel.getListItems(listName))
    val listItems by listViewModel.listData.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        listViewModel.getListItems(listName)
    }
    Scaffold(
        modifier = Modifier.padding(bottom = 40.dp),
        topBar = {
            Row (
                modifier = Modifier
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                TopAppBar(
                    title = { Text(listName) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Add your settings button here
                        IconButton(
                            modifier = Modifier.size(66.dp),
                            onClick = { /* Handle settings button click */ }
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
            ) { Text("Add Item") }
        }
    ) {
            // Display all items in the list
            LazyColumn (
                modifier = Modifier
                    .padding(top = 80.dp)
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
                                checked = listItem.isChecked!!,
                                onCheckedChange = {
                                    listViewModel
                                        .updateItemCheckedStatus(
                                            listName, listItem
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
                            ListItem(itemName, isChecked = false)
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
}
@Composable
fun DisplayAllListsScreen(
    listViewModel: ListViewModel,
    navController: NavController
) {
    val listItems by listViewModel.listData.collectAsStateWithLifecycle()

    // Observe changes in listData and recompose when it changes
    LaunchedEffect(listItems) {
        listViewModel.loadListData()
    }
    // Display the lists in a grid
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.padding(all = 8.dp)
    ) {
        if (listItems.isNotEmpty()) {
            // Display the lists here
            items(items = listItems) { listItem ->
                Button(
                    onClick = {
                        navController.navigate("add_item_screen/${listItem.itemName}")
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp)
                        .background(color = MaterialTheme.colorScheme.primary),
                    shape = CircleShape,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = (-2).dp
                    )
                ) {
                    Text(text = listItem.itemName ?: "",
                        color = MaterialTheme.colorScheme.onPrimary)
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