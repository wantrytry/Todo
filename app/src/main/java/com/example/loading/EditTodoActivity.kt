package com.example.loading

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView

class EditTodoActivity : Activity() {
    private var widgetId = -1
    private var todoId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetId = intent.getIntExtra("widget_id", -1)
        todoId = intent.getStringExtra("todo_id")

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            WindowManager.LayoutParams.FLAG_DIM_BEHIND
        )
        window.attributes.dimAmount = 0.5f

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            (dm.heightPixels * 0.6).toInt()
        )
        window.setGravity(Gravity.BOTTOM)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        setContentView(R.layout.activity_add_todo)

        val input = findViewById<EditText>(R.id.todo_input)
        val saveButton = findViewById<TextView>(R.id.save_button)
        val cancelButton = findViewById<TextView>(R.id.cancel_button)
        val title = findViewById<TextView>(R.id.dialog_title)
        title.text = "编辑待办"

        val todos = TodoPrefs.getTodos(this, widgetId)
        val todo = todos.find { it.id == todoId }
        if (todo != null) {
            input.setText(todo.text)
            input.setSelection(todo.text.length)
        }

        cancelButton.setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty() && todoId != null) {
                TodoPrefs.updateTodoText(this, todoId!!, text, widgetId)
                updateWidget()
                finish()
            }
        }
    }

    private fun updateWidget() {
        val intent = Intent(this, TodoWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, TodoWidget::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}
