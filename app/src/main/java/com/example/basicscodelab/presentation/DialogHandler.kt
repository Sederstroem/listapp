package com.example.basicscodelab.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DialogHandler {
    private var _isDialogVisible by mutableStateOf(false)
    val isDialogVisible: Boolean get() = _isDialogVisible

    fun showDialog() {
        _isDialogVisible = true
    }

    fun dismissDialog() {
        _isDialogVisible = false
    }
}