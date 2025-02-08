package com.babbira.studentspartner.ui.custom

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var scaleFactor = 1f
    private val scaleDetector: ScaleGestureDetector
    private val matrix = Matrix()
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    init {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                // Don't let the object get too small or too large
                scaleFactor = scaleFactor.coerceIn(0.1f, 10.0f)

                // Center the scaling
                matrix.setScale(scaleFactor, scaleFactor, 
                    width / 2f,  // Pivot X at center
                    height / 2f  // Pivot Y at center
                )
                imageMatrix = matrix
                invalidate()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && scaleFactor > 1f) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    matrix.postTranslate(dx, dy)
                    imageMatrix = matrix
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
        }
        return true
    }
} 