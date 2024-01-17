package com.example.basicscodelab.data

data class ListItem(
    var itemName: String? = null,
    var isChecked: Boolean? = false
    // Need to add something here to be able to delete an item from a list.
)