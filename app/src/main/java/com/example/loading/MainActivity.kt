package com.example.loading

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var todoListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "待办事项管理"
            textSize = 20f
            setPadding(0, 0, 0, 24)
        }
        layout.addView(title)

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val input = EditText(this).apply {
            hint = "输入待办事项"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        inputLayout.addView(input)

        val addButton = Button(this).apply {
            text = "添加"
            setOnClickListener {
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    TodoPrefs.addTodo(this@MainActivity, text)
                    input.text.clear()
                    refreshTodoList()
                    updateWidget()
                }
            }
        }
        inputLayout.addView(addButton)
        layout.addView(inputLayout)

        todoListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        layout.addView(todoListContainer)

        scrollView.addView(layout)
        setContentView(scrollView)

        refreshTodoList()
    }

    override fun onResume() {
        super.onResume()
        refreshTodoList()
    }

    private fun refreshTodoList() {
        todoListContainer.removeAllViews()

        val todos = TodoPrefs.getTodos(this)
        for (todo in todos) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            val checkbox = TextView(this).apply {
                text = if (todo.isCompleted) "✓" else "○"
                textSize = 18f
                setPadding(0, 0, 16, 0)
                setTextColor(
                    if (todo.isCompleted) android.graphics.Color.parseColor("#00C800")
                    else android.graphics.Color.GRAY
                )
                setOnClickListener {
                    TodoPrefs.toggleTodo(this@MainActivity, todo.id)
                    refreshTodoList()
                    updateWidget()
                }
            }
            row.addView(checkbox)

            val textView = TextView(this).apply {
                text = todo.text
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setTextColor(
                    if (todo.isCompleted) android.graphics.Color.GRAY
                    else android.graphics.Color.BLACK
                )
            }
            row.addView(textView)

            todoListContainer.addView(row)
        }

        if (todos.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "暂无待办事项"
                textSize = 14f
                setTextColor(android.graphics.Color.GRAY)
                setPadding(0, 24, 0, 0)
            }
            todoListContainer.addView(emptyText)
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