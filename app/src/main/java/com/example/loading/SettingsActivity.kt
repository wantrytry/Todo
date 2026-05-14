package com.example.loading

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class SettingsActivity : Activity() {
    private var widgetId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        widgetId = intent.getIntExtra("widget_id", -1)

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

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 20)
        }

        val titleBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "设置"
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#333333"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        titleBar.addView(title)

        val saveBtn = TextView(this).apply {
            text = "✓"
            textSize = 24f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            setPadding(8, 0, 0, 0)
            id = R.id.save_button
        }
        titleBar.addView(saveBtn)
        layout.addView(titleBar)

        val divider1 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                topMargin = 16
                bottomMargin = 16
            }
            setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
        }
        layout.addView(divider1)

        val transLabel = TextView(this).apply {
            text = "面板透明度"
            textSize = 15f
            setTextColor(android.graphics.Color.parseColor("#555555"))
            setPadding(0, 0, 0, 8)
            setLineSpacing(8f, 1f)
        }
        layout.addView(transLabel)

        val seekBar = SeekBar(this).apply {
            max = 255
            progress = TodoPrefs.getTransparency(this@SettingsActivity, widgetId)
            id = R.id.transparency_seekbar
        }
        layout.addView(seekBar)

        val transValue = TextView(this).apply {
            text = "${(seekBar.progress * 100 / 255)}%"
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#888888"))
            id = R.id.transparency_value
            setPadding(0, 4, 0, 0)
            setLineSpacing(8f, 1f)
        }
        layout.addView(transValue)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                transValue.text = "${(progress * 100 / 255)}%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val divider2 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                topMargin = 16
                bottomMargin = 16
            }
            setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
        }
        layout.addView(divider2)

        val hideCheckbox = CheckBox(this).apply {
            text = "隐藏已完成的待办（移至历史待办）"
            textSize = 15f
            setTextColor(android.graphics.Color.parseColor("#555555"))
            isChecked = TodoPrefs.getHideCompleted(this@SettingsActivity, widgetId)
            id = R.id.hide_completed_checkbox
            setPadding(0, 4, 0, 4)
            setLineSpacing(8f, 1f)
        }
        layout.addView(hideCheckbox)

        val divider3 = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).apply {
                topMargin = 16
                bottomMargin = 16
            }
            setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
        }
        layout.addView(divider3)

        val historyLabel = TextView(this).apply {
            text = "历史待办"
            textSize = 15f
            setTextColor(android.graphics.Color.parseColor("#555555"))
            setPadding(0, 0, 0, 4)
            setLineSpacing(8f, 1f)
        }
        layout.addView(historyLabel)

        val historyHint = TextView(this).apply {
            text = "点击圆圈恢复到待办面板"
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#999999"))
            setPadding(0, 0, 0, 8)
            setLineSpacing(8f, 1f)
        }
        layout.addView(historyHint)

        val historyContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            id = View.generateViewId()
        }
        layout.addView(historyContainer)

        val todos = TodoPrefs.getTodos(this, widgetId).filter { it.isHistory }
        if (todos.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "暂无历史待办"
                textSize = 13f
                setTextColor(android.graphics.Color.parseColor("#BBBBBB"))
                setPadding(0, 8, 0, 0)
                setLineSpacing(8f, 1f)
            }
            historyContainer.addView(emptyText)
        } else {
            for (todo in todos) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 8, 0, 8)
                }

                val checkbox = TextView(this).apply {
                    text = "○"
                    textSize = 18f
                    setTextColor(android.graphics.Color.parseColor("#FFDB58"))
                    setPadding(0, 0, 12, 0)
                    setOnClickListener {
                        TodoPrefs.restoreFromHistory(this@SettingsActivity, todo.id, widgetId)
                        updateWidget()
                        recreate()
                    }
                }
                row.addView(checkbox)

                val textView = TextView(this).apply {
                    text = todo.text
                    textSize = 14f
                    setTextColor(android.graphics.Color.parseColor("#888888"))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setLineSpacing(8f, 1f)
                }
                row.addView(textView)

                historyContainer.addView(row)
            }
        }

        scrollView.addView(layout)
        setContentView(scrollView)

        saveBtn.setOnClickListener {
            val transparency = seekBar.progress
            val hideCompleted = hideCheckbox.isChecked

            TodoPrefs.setTransparency(this, transparency, widgetId)
            TodoPrefs.setHideCompleted(this, hideCompleted, widgetId)

            if (hideCompleted) {
                val todos = TodoPrefs.getTodos(this, widgetId)
                for (todo in todos) {
                    if (todo.isCompleted && !todo.isHistory) {
                        TodoPrefs.moveToHistory(this, todo.id, widgetId)
                    }
                }
            }

            updateWidget()
            finish()
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
