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
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView

class SettingsActivity : Activity() {
    private lateinit var transparencySeekBar: SeekBar
    private lateinit var transparencyValue: TextView
    private lateinit var hideCompletedCheckbox: CheckBox
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            (dm.heightPixels * 0.45).toInt()
        )
        window.setGravity(Gravity.BOTTOM)

        setContentView(R.layout.activity_settings)

        transparencySeekBar = findViewById(R.id.transparency_seekbar)
        transparencyValue = findViewById(R.id.transparency_value)
        hideCompletedCheckbox = findViewById(R.id.hide_completed_checkbox)
        saveButton = findViewById(R.id.save_button)

        val currentTransparency = TodoPrefs.getTransparency(this)
        transparencySeekBar.progress = currentTransparency
        transparencyValue.text = "${(currentTransparency * 100 / 255)}%"

        hideCompletedCheckbox.isChecked = TodoPrefs.getHideCompleted(this)

        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                transparencyValue.text = "${(progress * 100 / 255)}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            val transparency = transparencySeekBar.progress
            val hideCompleted = hideCompletedCheckbox.isChecked

            TodoPrefs.setTransparency(this, transparency)
            TodoPrefs.setHideCompleted(this, hideCompleted)

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