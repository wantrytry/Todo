package com.example.loading

data class TodoItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false
)