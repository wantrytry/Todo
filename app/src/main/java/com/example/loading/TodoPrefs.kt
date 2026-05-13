package com.example.loading

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TodoPrefs {
    private const val PREFS_NAME = "TodoPrefs"
    private const val KEY_TODOS = "todos"
    private const val KEY_TRANSPARENCY = "transparency"
    private const val KEY_HIDE_COMPLETED = "hide_completed"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTodos(context: Context): List<TodoItem> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_TODOS, "[]") ?: "[]"
        val type = object : TypeToken<List<TodoItem>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun saveTodos(context: Context, todos: List<TodoItem>) {
        val prefs = getPrefs(context)
        val json = Gson().toJson(todos)
        prefs.edit().putString(KEY_TODOS, json).apply()
    }

    fun addTodo(context: Context, text: String) {
        val todos = getTodos(context).toMutableList()
        val newTodo = TodoItem(
            id = System.currentTimeMillis().toString(),
            text = text,
            isCompleted = false
        )
        todos.add(newTodo)
        saveTodos(context, todos)
    }

    fun toggleTodo(context: Context, id: String) {
        val todos = getTodos(context).map { todo ->
            if (todo.id == id) {
                todo.copy(isCompleted = !todo.isCompleted)
            } else {
                todo
            }
        }
        saveTodos(context, todos)
    }

    fun getTransparency(context: Context): Int {
        val prefs = getPrefs(context)
        return prefs.getInt(KEY_TRANSPARENCY, 255)
    }

    fun setTransparency(context: Context, transparency: Int) {
        val prefs = getPrefs(context)
        prefs.edit().putInt(KEY_TRANSPARENCY, transparency).apply()
    }

    fun getHideCompleted(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_HIDE_COMPLETED, false)
    }

    fun setHideCompleted(context: Context, hide: Boolean) {
        val prefs = getPrefs(context)
        prefs.edit().putBoolean(KEY_HIDE_COMPLETED, hide).apply()
    }

    fun getPendingCount(context: Context): Int {
        return getTodos(context).count { !it.isCompleted }
    }
}