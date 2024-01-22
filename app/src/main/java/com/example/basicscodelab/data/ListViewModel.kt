package com.example.basicscodelab.data

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.basicscodelab.sign_in.UserData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListViewModel : ViewModel() {
    private val rootRef: DatabaseReference = Firebase
        .database("https://basicscodelab-72f56-default-rtdb.europe-west1.firebasedatabase.app/")
        .getReference("users")

    // LiveData to observe changes in the list data
    private val _listData = MutableStateFlow<List<ListItem>>(emptyList())
    val listData: StateFlow<List<ListItem>> get() = _listData

    fun createNewList(listName: String, user: UserData) {
        user.userName?.let {
            rootRef
                .child(it)
                .child("lists")
                .child(listName)
                .setValue(listName)
        }
    }

    // TODO Somehow it adds a checked boolean into the database.
    //  FIX IT!!!!!
    fun addItemToList(listName: String, itemName: String, user: UserData) {
        user.userName?.let { it ->
            val listRef = rootRef.child(it).child("lists").child(listName)

            // Generate a unique key for the new item
            val newItemKey = listRef.push().key

            // Set the new item under the list
            newItemKey?.let {
                listRef.child(it).setValue(ListItem(itemName))
            }
        }
    }
//    // TODO continue with this method. Need to modify!!!!!!!
//    fun getList(listName: String) {
//        rootRef.child(listName).get()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    _listData.value =
//                        (task.result.getValue<List<ListItem>>())!!
//                } else {
//                    Log.w(TAG, task.exception?.localizedMessage.toString())
//                }
//            }
//        // Implement this method when the user clicks on a list.
//        // Read the appropriate list from the database.
//    }

    // Need to change return type of this method to Flow or
    // LiveData to update it in when the composable that uses the function recomposes.
    fun getListItems(listName: String, user: UserData) {
        rootRef.child(user.userName ?: "").child("lists").child(listName)
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
    private fun addUserToDatabase(user: UserData) {
        // Check if the user already exists in the database
        rootRef.child(user.userName ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val userMap = mapOf(
                        "userName" to user.userName,
                        "userId" to user.userId
                    )
                    // User does not exist, add them to the database
                    rootRef.child(user.userName ?: "").setValue(userMap)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }
    fun handleUserLogin(user: UserData?) {
        if (user != null) {
            addUserToDatabase(user)
        }
        // You can add other logic related to user login here
    }
    fun loadListData(user: UserData) {
        val listItems = mutableListOf<ListItem>()

        rootRef.child(user.userName ?: "").child("lists").addListenerForSingleValueEvent(object : ValueEventListener {
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
        listName: String, item: ListItem, user: UserData
    ) {
        rootRef.child(user.userName ?: "")
            .child("lists")
            .child(listName)
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
    // TODO continue with the shareList method!!!!!!!!!!!
    fun shareList(
        listId: String, sharedUserId: String, ownerUser: UserData
    ) {
//        val ownerId = auth.currentUser?.run { UserData(
//            userId = uid,
//            userName = displayName,
//            profilePictureUrl = photoUrl?.toString()) }
        // Log to verify that the method is called
        Log.d(TAG, "shareList called: listId=$listId, sharedUserId=$sharedUserId, ownerId=${ownerUser.userId}")

        // Check if the shared user exists
        rootRef.child(sharedUserId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val sharedUserExists = task.result.exists()
                Log.d(TAG, "Found the user?!?: listId=$listId, sharedUserId=$sharedUserId, ownerId=${ownerUser.userId}")


                if (sharedUserExists && sharedUserId != ownerUser.userName) {
                    // Perform the share operation

                    // Step 1: Add the list to the shared user's lists

                    rootRef.child(sharedUserId)
                        .child("lists")
                        .child(listId).setValue(listId)

                    // Step 2: Add the shared user to the list of shared users for this list
                    rootRef.child(ownerUser.userName ?: "")
                        .child("lists")
                        .child(listId)
                        .child("sharing")
                        .child(sharedUserId).setValue(true)

                    // Optionally, you can add additional logic here.

                    Log.d(TAG, "List shared successfully!?!")
                } else {
                    Log.w(TAG, "The user with id $sharedUserId does not exist.")
                }
            } else {
                Log.w(TAG, "Error checking if the user exists: ${task.exception?.localizedMessage}")
            }
        }
    }
}