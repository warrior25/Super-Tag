package com.huikka.supertag

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider

class CustomSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val slider: Slider
    private val name: TextView
    private val value: TextView


    init {
        LayoutInflater.from(context).inflate(R.layout.custom_slider, this, true)
        slider = findViewById(R.id.slider)
        name = findViewById(R.id.name)
        value = findViewById(R.id.value)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.CustomSlider, 0, 0)
            name.text = typedArray.getString(R.styleable.CustomSlider_customName)
            slider.value = typedArray.getFloat(R.styleable.CustomSlider_customValue, 30f)
            slider.valueTo = typedArray.getFloat(R.styleable.CustomSlider_customValueTo, 100f)
            slider.valueFrom = typedArray.getFloat(R.styleable.CustomSlider_customValueFrom, 0f)
            slider.stepSize = typedArray.getFloat(R.styleable.CustomSlider_customStepSize, 1f)
            typedArray.recycle()

            value.text = slider.value.toInt().toString()


        }
        slider.addOnChangeListener { slider, _, _ ->
            value.text = slider.value.toInt().toString()
        }
    }

    fun getValue(): Float {
        return slider.value

    }

    fun setValue(value: Float) {
        slider.value = value
    }

}
