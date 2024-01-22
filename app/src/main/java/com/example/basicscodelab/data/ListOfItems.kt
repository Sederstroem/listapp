package com.example.basicscodelab.data

import java.util.UUID

data class ListOfItems(
    val listName: String? = null,
    val itemsInList: List<ListItem> = emptyList(),
    val listId: UUID = UUID.randomUUID()
)
