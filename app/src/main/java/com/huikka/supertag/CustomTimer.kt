package com.huikka.supertag

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat

class CustomTimer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconImageView: ImageView
    private val chronometer: Chronometer
    private var callback: () -> Unit = {}


    init {
        LayoutInflater.from(context).inflate(R.layout.custom_timer, this, true)
        iconImageView = findViewById(R.id.custom_item_icon)
        chronometer = findViewById(R.id.custom_item_time)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomTimer, 0, 0)
            val icon = typedArray.getDrawable(R.styleable.CustomTimer_customIcon)
            typedArray.recycle()

            setIcon(icon ?: ContextCompat.getDrawable(context, R.drawable.runner))
        }

        chronometer.setOnChronometerTickListener {
            val timeLeft = SystemClock.elapsedRealtime() - chronometer.base
            if (timeLeft > 0) {
                stopTimer()
                callback.invoke()
            }
        }
    }

    fun setTime(ms: Long) {
        chronometer.base = SystemClock.elapsedRealtime() + ms
    }

    fun startTimer(onTimeout: () -> Unit) {
        chronometer.start()
        callback = onTimeout
    }

    fun stopTimer() {
        chronometer.stop()
    }

    fun setIcon(icon: Drawable?) {
        iconImageView.setImageDrawable(icon)
    }

}
