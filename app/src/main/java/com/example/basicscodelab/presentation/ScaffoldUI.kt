package com.example.basicscodelab.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.basicscodelab.data.ListViewModel

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