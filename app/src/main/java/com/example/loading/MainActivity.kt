package com.example.loading

import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
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
            (dm.widthPixels * 0.85).toInt(),
            (dm.heightPixels * 0.8).toInt()
        )
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawableResource(R.drawable.dialog_rounded)

        val scrollView = ScrollView(this).apply {
            setBackgroundResource(R.drawable.dialog_rounded)
            isVerticalScrollBarEnabled = false
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val title = TextView(this).apply {
            text = "Loading 待办小部件"
            textSize = 22f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.parseColor("#333333"))
            setPadding(0, 0, 0, 20)
        }
        layout.addView(title)

        val features = listOf(
            "📋 功能说明" to "",
            "" to "1. 桌面待办小部件：长按桌面空白处，选择「小部件」，找到 Loading 添加到桌面即可使用。",
            "" to "2. 调整大小：长按小部件，拖动边缘的白色圆点即可自由调整面板大小。",
            "" to "3. 新建待办：点击右上角「+」号，在弹出的输入框中输入待办内容，点击「✓」保存。",
            "" to "4. 完成待办：点击待办前面的圆圈，圆圈填充为金色（RGB 255,219,88）并显示白色「✓」，文字变灰并加删除线。",
            "" to "5. 编辑待办：点击待办文字内容，可弹出编辑页面修改待办内容。",
            "" to "6. 隐藏已完成：在设置中开启「隐藏已完成的待办」，已完成的待办会自动移至历史待办列表。",
            "" to "7. 恢复待办：在设置页面的「历史待办」中，点击金色圆圈即可恢复到待办面板。",
            "" to "8. 设置功能：点击右上角「⚙」符号，可调节面板透明度、隐藏已完成待办等。",
            "" to "9. 多个小部件：每个小部件独立管理自己的待办列表和设置，互不影响。",
            "" to "10. 删除小部件：长按小部件拖到删除区域即可，数据会自动清理。"
        )

        for ((heading, content) in features) {
            if (heading.isNotEmpty()) {
                val headingView = TextView(this).apply {
                    text = heading
                    textSize = 17f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor("#FFDB58"))
                    setPadding(0, 16, 0, 8)
                    setLineSpacing(8f, 1f)
                }
                layout.addView(headingView)
            }
            if (content.isNotEmpty()) {
                val contentView = TextView(this).apply {
                    text = content
                    textSize = 15f
                    setTextColor(android.graphics.Color.parseColor("#555555"))
                    setPadding(0, 6, 0, 6)
                    setLineSpacing(8f, 1f)
                }
                layout.addView(contentView)
            }
        }

        val closeBtn = TextView(this).apply {
            text = "关闭"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#FFDB58"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 0)
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
        }
        layout.addView(closeBtn)

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
