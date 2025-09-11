package com.airscout.airscout32.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.airscout.airscout32.R

class BatteryIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var batteryLevel: Float = 0f // 0-100
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val batteryRect = RectF()
    private val fillRect = RectF()
    private val tipRect = RectF()
    
    init {
        textPaint.apply {
            textAlign = Paint.Align.CENTER
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.WHITE
        }
    }
    
    fun setBatteryLevel(level: Float) {
        batteryLevel = level.coerceIn(0f, 100f)
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        
        // Kompakte Battery-Icon Dimensionen
        val batteryWidth = width * 0.65f
        val batteryHeight = height * 0.5f
        val tipWidth = width * 0.1f
        val tipHeight = height * 0.25f
        
        val startX = (width - batteryWidth - tipWidth) / 2
        val startY = (height - batteryHeight) / 2
        
        // Battery outline
        batteryRect.set(startX, startY, startX + batteryWidth, startY + batteryHeight)
        
        // Battery tip
        tipRect.set(
            startX + batteryWidth, 
            startY + (batteryHeight - tipHeight) / 2,
            startX + batteryWidth + tipWidth,
            startY + (batteryHeight + tipHeight) / 2
        )
        
        // Draw battery outline
        paint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = Color.WHITE
        }
        canvas.drawRect(batteryRect, paint)
        canvas.drawRect(tipRect, paint)
        
        // Draw battery fill
        val fillWidth = (batteryWidth - 4f) * (batteryLevel / 100f)
        fillRect.set(
            startX + 2f,
            startY + 2f,
            startX + 2f + fillWidth,
            startY + batteryHeight - 2f
        )
        
        paint.apply {
            style = Paint.Style.FILL
            color = when {
                batteryLevel >= 20f -> Color.GREEN
                else -> Color.RED
            }
        }
        canvas.drawRect(fillRect, paint)
        
        // Draw percentage text inside battery if there's space
        if (batteryWidth > 20f) {
            val percentText = "${batteryLevel.toInt()}%"
            val textY = startY + batteryHeight / 2 + textPaint.textSize / 3
            canvas.drawText(percentText, startX + batteryWidth / 2, textY, textPaint)
        }
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 70
        val desiredHeight = 32
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }
        
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }
        
        setMeasuredDimension(width, height)
    }
}
