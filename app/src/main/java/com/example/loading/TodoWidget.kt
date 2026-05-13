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

        if (intent.action == "com.example.loading.TOGGLE_TODO") {
            val todoId = intent.getStringExtra("todo_id")
            if (todoId != null) {
                TodoPrefs.toggleTodo(context, todoId)
                updateAllWidgets(context)
            }
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

    val pendingCount = TodoPrefs.getPendingCount(context)
    views.setTextViewText(R.id.pending_count, "${pendingCount}项待办")

    val transparency = TodoPrefs.getTransparency(context)
    views.setInt(R.id.widget_background, "setImageAlpha", transparency)

    val buttonTextColor = Color.argb(transparency, 102, 102, 102)
    views.setTextColor(R.id.add_button, buttonTextColor)
    views.setTextColor(R.id.settings_button, buttonTextColor)

    val addIntent = Intent(context, AddTodoActivity::class.java)
    val addPendingIntent = PendingIntent.getActivity(
        context,
        0,
        addIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.add_button, addPendingIntent)

    val settingsIntent = Intent(context, SettingsActivity::class.java)
    val settingsPendingIntent = PendingIntent.getActivity(
        context,
        1,
        settingsIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.settings_button, settingsPendingIntent)

    val serviceIntent = Intent(context, TodoWidgetService::class.java)
    serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    serviceIntent.setData(android.net.Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)))
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        views.setRemoteAdapter(R.id.todo_list, serviceIntent)
    }

    val templateIntent = Intent(context, TodoWidget::class.java)
    templateIntent.action = "com.example.loading.TOGGLE_TODO"
    val templatePendingIntent = PendingIntent.getBroadcast(
        context,
        2,
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