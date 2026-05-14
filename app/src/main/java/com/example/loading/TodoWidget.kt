package com.example.loading

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

class TodoWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            "com.example.loading.TOGGLE_TODO" -> {
                val todoId = intent.getStringExtra("todo_id")
                val widgetId = intent.getIntExtra("widget_id", -1)
                val clickType = intent.getStringExtra("click_type") ?: "checkbox"
                if (todoId != null) {
                    if (clickType == "text") {
                        val editIntent = Intent(context, EditTodoActivity::class.java)
                        editIntent.putExtra("widget_id", widgetId)
                        editIntent.putExtra("todo_id", todoId)
                        editIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(editIntent)
                    } else {
                        val hideCompleted = TodoPrefs.getHideCompleted(context, widgetId)
                        val todos = TodoPrefs.getTodos(context, widgetId)
                        val todo = todos.find { it.id == todoId }
                        if (todo != null) {
                            if (todo.isCompleted) {
                                TodoPrefs.moveToHistory(context, todoId, widgetId)
                            } else {
                                TodoPrefs.toggleTodo(context, todoId, widgetId)
                                if (hideCompleted) {
                                    TodoPrefs.moveToHistory(context, todoId, widgetId)
                                }
                            }
                        }
                        updateAllWidgets(context)
                    }
                }
            }
            "com.example.loading.MOVE_TO_HISTORY" -> {
                val todoId = intent.getStringExtra("todo_id")
                val widgetId = intent.getIntExtra("widget_id", -1)
                if (todoId != null) {
                    TodoPrefs.moveToHistory(context, todoId, widgetId)
                    updateAllWidgets(context)
                }
            }
            "com.example.loading.RESTORE_FROM_HISTORY" -> {
                val todoId = intent.getStringExtra("todo_id")
                val widgetId = intent.getIntExtra("widget_id", -1)
                if (todoId != null) {
                    TodoPrefs.restoreFromHistory(context, todoId, widgetId)
                    updateAllWidgets(context)
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            TodoPrefs.deleteWidgetPrefs(context, id)
        }
    }

    override fun onEnabled(context: Context) {}

    override fun onDisabled(context: Context) {}

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TodoWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, TodoWidget::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.todo_widget)

    val pendingCount = TodoPrefs.getPendingCount(context, appWidgetId)
    views.setTextViewText(R.id.pending_count, "${pendingCount}项待办")

    val transparency = TodoPrefs.getTransparency(context, appWidgetId)
    views.setInt(R.id.widget_background, "setImageAlpha", transparency)
    views.setInt(R.id.widget_background, "setColorFilter", Color.argb(255, 255, 255, 255))

    val addIntent = Intent(context, AddTodoActivity::class.java)
    addIntent.putExtra("widget_id", appWidgetId)
    val addPendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId * 10,
        addIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.add_button, addPendingIntent)

    val settingsIntent = Intent(context, SettingsActivity::class.java)
    settingsIntent.putExtra("widget_id", appWidgetId)
    val settingsPendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId * 10 + 1,
        settingsIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.settings_button, settingsPendingIntent)

    val serviceIntent = Intent(context, TodoWidgetService::class.java)
    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    serviceIntent.putExtra("widget_id", appWidgetId)
    serviceIntent.setData(android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)))
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        views.setRemoteAdapter(R.id.todo_list, serviceIntent)
    }

    val templateIntent = Intent(context, TodoWidget::class.java)
    templateIntent.action = "com.example.loading.TOGGLE_TODO"
    val templatePendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId * 10 + 2,
        templateIntent,
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )
    views.setPendingIntentTemplate(R.id.todo_list, templatePendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.todo_list)
    }
}
