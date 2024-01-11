package com.example.basicscodelab

data class ListOfItems(
    val listName: String? = null,
    val itemsInList: List<ListItem> = emptyList()
)
