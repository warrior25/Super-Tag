package com.huikka.supertag.ui.components

import android.os.CountDownTimer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huikka.supertag.data.helpers.TimeConverter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Timer(
    startTime: Long,
    totalTime: Long,
    handleColor: Color,
    inactiveBarColor: Color,
    activeBarColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 5.dp,
    title: String
) {
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    var value by remember {
        mutableFloatStateOf(1f)
    }
    var currentTime by remember {
        mutableLongStateOf(0L)
    }

    val countDownTimer = object : CountDownTimer(startTime, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            currentTime = millisUntilFinished
            value = currentTime / totalTime.toFloat()
        }

        override fun onFinish() {

        }
    }

    DisposableEffect(true) {
        countDownTimer.start()
        onDispose {
            countDownTimer.cancel()
        }
    }

    LaunchedEffect(startTime) {
        countDownTimer.cancel()
        countDownTimer.start()
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier.onSizeChanged { size = it }) {
        Canvas(modifier = modifier) {
            drawArc(
                color = inactiveBarColor,
                startAngle = -215f,
                sweepAngle = 250f,
                useCenter = false,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = activeBarColor,
                startAngle = -215f,
                sweepAngle = 250f * value,
                useCenter = false,
                size = Size(size.width.toFloat(), size.height.toFloat()),
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            val center = Offset(size.width / 2f, size.height / 2f)
            val beta = (250f * value + 145f) * (PI / 180f).toFloat()
            val r = size.width / 2f
            val a = cos(beta) * r
            val b = sin(beta) * r
            drawPoints(
                listOf(Offset(center.x + a, center.y + b)),
                pointMode = PointMode.Points,
                color = handleColor,
                strokeWidth = (strokeWidth * 3f).toPx(),
                cap = StrokeCap.Round
            )
        }
        Text(
            text = minutesAndSeconds(currentTime),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset(y = (-4).dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.offset(y = 28.dp)
        )
    }
}

fun minutesAndSeconds(time: Long): String {
    val result = TimeConverter.longToMinutesAndSeconds(time)
    if (result.first == 0L) {
        return "${result.second} s"
    }
    return "${result.first} m\n${result.second} s"
}