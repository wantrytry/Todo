package com.example.loading

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TodoPrefs {
    private const val PREFS_NAME = "TodoPrefs"
    private const val KEY_TODOS = "todos"
    private const val KEY_HIDE_COMPLETED = "hide_completed"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getWidgetPrefs(context: Context, appWidgetId: Int): SharedPreferences {
        return context.getSharedPreferences("widget_$appWidgetId", Context.MODE_PRIVATE)
    }

    fun getTodos(context: Context, appWidgetId: Int = -1): List<TodoItem> {
        val prefs = if (appWidgetId >= 0) getWidgetPrefs(context, appWidgetId) else getPrefs(context)
        val json = prefs.getString(KEY_TODOS, "[]") ?: "[]"
        val type = object : TypeToken<List<TodoItem>>() {}.type
        return Gson().fromJson(json, type) ?: emptyList()
    }

    fun saveTodos(context: Context, todos: List<TodoItem>, appWidgetId: Int = -1) {
        val prefs = if (appWidgetId >= 0) getWidgetPrefs(context, appWidgetId) else getPrefs(context)
        val json = Gson().toJson(todos)
        prefs.edit().putString(KEY_TODOS, json).apply()
    }

    fun addTodo(context: Context, text: String, appWidgetId: Int = -1) {
        val todos = getTodos(context, appWidgetId).toMutableList()
        val newTodo = TodoItem(
            id = System.currentTimeMillis().toString(),
            text = text,
            isCompleted = false
        )
        todos.add(newTodo)
        saveTodos(context, todos, appWidgetId)
    }

    fun updateTodoText(context: Context, id: String, newText: String, appWidgetId: Int = -1) {
        val todos = getTodos(context, appWidgetId).map { todo ->
            if (todo.id == id) {
                todo.copy(text = newText)
            } else {
                todo
            }
        }
        saveTodos(context, todos, appWidgetId)
    }

    fun toggleTodo(context: Context, id: String, appWidgetId: Int = -1) {
        val todos = getTodos(context, appWidgetId).map { todo ->
            if (todo.id == id) {
                todo.copy(isCompleted = !todo.isCompleted)
            } else {
                todo
            }
        }
        saveTodos(context, todos, appWidgetId)
    }

    fun moveToHistory(context: Context, id: String, appWidgetId: Int = -1) {
        val todos = getTodos(context, appWidgetId).map { todo ->
            if (todo.id == id) {
                todo.copy(isHistory = true, isCompleted = false)
            } else {
                todo
            }
        }
        saveTodos(context, todos, appWidgetId)
    }

    fun restoreFromHistory(context: Context, id: String, appWidgetId: Int = -1) {
        val todos = getTodos(context, appWidgetId).map { todo ->
            if (todo.id == id) {
                todo.copy(isHistory = false)
            } else {
                todo
            }
        }
        saveTodos(context, todos, appWidgetId)
    }

    fun getTransparency(context: Context, appWidgetId: Int = -1): Int {
        val prefs = if (appWidgetId >= 0) getWidgetPrefs(context, appWidgetId) else getPrefs(context)
        return prefs.getInt("transparency", 255)
    }

    fun setTransparency(context: Context, transparency: Int, appWidgetId: Int = -1) {
        val prefs = if (appWidgetId >= 0) getWidgetPrefs(context, appWidgetId) else getPrefs(context)
        prefs.edit().putInt("transparency", transparency).apply()
    }

    fun getHideCompleted(context: Context, appWidgetId: Int = -1): Boolean {
        val prefs = if (appWidgetId >= 0) getWidgetPrefs(context, appWidgetId) else getPrefs(context)
        return prefs.getBoolean(KEY_HIDE_COMPLETED, false)
    }

    fun setHideCompleted(context: Context, hide: Boolean, appWidgetId: Int = -1) {
        val prefs = if (appWidgetId >= 0) getWidgetPrefs(context, appWidgetId) else getPrefs(context)
        prefs.edit().putBoolean(KEY_HIDE_COMPLETED, hide).apply()
    }

    fun getPendingCount(context: Context, appWidgetId: Int = -1): Int {
        return getTodos(context, appWidgetId).count { !it.isCompleted && !it.isHistory }
    }

    fun deleteWidgetPrefs(context: Context, appWidgetId: Int) {
        getWidgetPrefs(context, appWidgetId).edit().clear().apply()
    }
}
