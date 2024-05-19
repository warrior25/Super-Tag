package com.huikka.supertag

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(Build.VERSION_CODES.O)
class CustomTimer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconImageView: ImageView
    private val chronometer: Chronometer


    init {
        LayoutInflater.from(context).inflate(R.layout.custom_timer, this, true)
        iconImageView = findViewById(R.id.custom_item_icon)
        chronometer = findViewById(R.id.custom_item_time)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomTimer, 0, 0)
            val minutes = typedArray.getInt(R.styleable.CustomTimer_customTime, 5)
            val icon = typedArray.getDrawable(R.styleable.CustomTimer_customIcon)
            typedArray.recycle()

            setTime(minutes)
            setIcon(icon ?: ContextCompat.getDrawable(context, R.drawable.runner))
        }

        chronometer.setOnChronometerTickListener {
            val timeLeft = SystemClock.elapsedRealtime() - chronometer.base
            if (timeLeft > 0) {
                stopTimer()
                Log.d("TAG", "Stop")

            }
        }

        startTimer()
    }

    fun setTime(minutes: Int) {
        chronometer.base = (SystemClock.elapsedRealtime() + (minutes * 1000))
    }

    fun startTimer() {
        chronometer.start()
    }

    fun stopTimer() {
        chronometer.stop()
    }

    fun setIcon(icon: Drawable?) {
        iconImageView.setImageDrawable(icon)
    }

}
