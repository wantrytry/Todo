package com.example.loading

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
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
        todos = TodoPrefs.getTodos(context, appWidgetId)
        hideCompleted = TodoPrefs.getHideCompleted(context, appWidgetId)
    }

    override fun onDestroy() {}

    override fun getCount(): Int {
        val displayTodos = todos.filter { !it.isHistory }
        return if (hideCompleted) {
            displayTodos.count { !it.isCompleted }
        } else {
            displayTodos.size
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        val displayTodos = todos.filter { !it.isHistory }
        val filtered = if (hideCompleted) {
            displayTodos.filter { !it.isCompleted }
        } else {
            displayTodos.sortedWith(compareBy({ it.isCompleted }, { it.id }))
        }

        val todo = filtered[position]
        val views = RemoteViews(context.packageName, R.layout.todo_item)

        views.setTextViewText(R.id.todo_text, todo.text)

        if (todo.isCompleted) {
            views.setTextViewText(R.id.todo_checkbox, "✓")
            views.setTextColor(R.id.todo_checkbox, Color.argb(255, 255, 255, 255))
            views.setInt(R.id.todo_checkbox, "setBackgroundResource", R.drawable.circle_filled)

            views.setTextColor(R.id.todo_text, Color.parseColor("#FF888888"))
            val spannable = SpannableString(todo.text)
            spannable.setSpan(StrikethroughSpan(), 0, todo.text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            views.setTextViewText(R.id.todo_text, spannable)
        } else {
            views.setTextViewText(R.id.todo_checkbox, "")
            views.setInt(R.id.todo_checkbox, "setBackgroundResource", R.drawable.circle_empty)

            views.setTextColor(R.id.todo_text, Color.parseColor("#FF333333"))
        }

        val checkboxIntent = Intent().apply {
            putExtra("todo_id", todo.id)
            putExtra("click_type", "checkbox")
            putExtra("widget_id", appWidgetId)
        }
        views.setOnClickFillInIntent(R.id.todo_checkbox, checkboxIntent)

        val textIntent = Intent().apply {
            putExtra("todo_id", todo.id)
            putExtra("click_type", "text")
            putExtra("widget_id", appWidgetId)
        }
        views.setOnClickFillInIntent(R.id.todo_text, textIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 2

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
