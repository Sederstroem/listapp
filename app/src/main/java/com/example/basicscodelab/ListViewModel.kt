package com.example.basicscodelab

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Flow

class ListViewModel : ViewModel() {
    private val rootRef: DatabaseReference = Firebase
        .database("https://basicscodelab-72f56-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("lists")

    // LiveData or State to observe changes in the list data
//    private val _listData = mutableStateOf(emptyList<ListItem>())
//    val listData: State<List<ListItem>> get() = _listData

    // LiveData to observe changes in the list data
    private val _listData = MutableStateFlow<List<ListItem>>(emptyList())
    val listData: StateFlow<List<ListItem>> get() = _listData

    // TODO it only adds the list to the database when the user then adds an item!
    //  FIX IT!!!!!
    fun createNewList(listName: String) {
        rootRef.child(listName).setValue(null)
    }

    // TODO continue with this method. Need to modify!!!!!!!
    fun getList(listName: String) {
        rootRef.child(listName).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _listData.value =
                        (task.result.getValue<List<ListItem>>())!!
                } else {
                    Log.w(TAG, task.exception?.localizedMessage.toString())
                }
            }
        // Implement this method when the user clicks on a list.
        // Read the appropriate list from the database.
    }

    // TODO Somehow it adds a checked boolean into the database.
    //  FIX IT!!!!!
    fun addItemToList(listName: String, item: ListItem) {
        // Get a reference to the list node in the database
        val listRef = rootRef.child(listName)
        val newItemKey = listRef.push()
        newItemKey.setValue(item)
    }

    // Need to change return type of this method to Flow or
    // LiveData to update it in when the composable that uses the function recomposes.
    fun getListItems(listName: String) {
        rootRef.child(listName)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val itemList = mutableListOf<ListItem>()

                    for (childSnapshot in snapshot.children) {
                        val itemName = childSnapshot.child("itemName").value as String
                        val isCheckedValue = childSnapshot.child("isChecked").value
                        val isChecked = isCheckedValue as? Boolean ?: false

                        val listItem = ListItem(itemName = itemName, isChecked)
                        itemList.add(listItem)
                    }

                    // Update the StateFlow with the new data
                    _listData.value = itemList
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors
                }
            })
    }
    fun loadListData() {
        val listItems = mutableListOf<ListItem>()

        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    // Perform a null check before casting to String
                    val listName = childSnapshot.key
                    Log.w(TAG, listName.toString())
                    if (listName != null) {
                        val listItem = ListItem(itemName = listName)
                        listItems.add(listItem)
                    } else {
                        // Handle the case where "listName" is not a String or is null
                    }
                }

                _listData.value = listItems
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }


    fun updateItemCheckedStatus(
        listName: String, item: ListItem ) {
        rootRef.child(listName)
            .orderByChild("itemName")
            .equalTo(item.itemName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.child("isChecked").setValue(!item.isChecked!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors
                }
            })
    }
}