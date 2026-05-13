package com.example.loading

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class TodoRemoteViewsFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var todos = listOf<TodoItem>()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var hideCompleted = false

    override fun onCreate() {
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        todos = TodoPrefs.getTodos(context)
        hideCompleted = TodoPrefs.getHideCompleted(context)
    }

    override fun onDestroy() {}

    override fun getCount(): Int {
        return if (hideCompleted) {
            todos.count { !it.isCompleted }
        } else {
            todos.size
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        val displayTodos = if (hideCompleted) {
            todos.filter { !it.isCompleted }
        } else {
            todos
        }

        val todo = displayTodos[position]
        val views = RemoteViews(context.packageName, R.layout.todo_item)

        views.setTextViewText(R.id.todo_text, todo.text)

        if (todo.isCompleted) {
            views.setTextViewText(R.id.todo_checkbox, "●✓")
            views.setTextColor(R.id.todo_checkbox, Color.parseColor("#FFFF8C00"))
            views.setTextColor(R.id.todo_text, Color.parseColor("#FF555555"))
        } else {
            views.setTextViewText(R.id.todo_checkbox, "○")
            views.setTextColor(R.id.todo_checkbox, Color.parseColor("#FF999999"))
            views.setTextColor(R.id.todo_text, Color.parseColor("#FF333333"))
        }

        val fillInIntent = Intent().apply {
            putExtra("todo_id", todo.id)
            putExtra("action", "toggle")
        }
        views.setOnClickFillInIntent(R.id.todo_item_layout, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}