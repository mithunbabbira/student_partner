package com.babbira.studentspartner.ui.custom

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var matrix = Matrix()
    private var matrixValues = FloatArray(9)
    private var scale = 1f
    private var minScale = 1f
    private var maxScale = 5f
    
    private var prevX = 0f
    private var prevY = 0f
    private var activePointerId = INVALID_POINTER_ID

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = max(minScale, min(scale, maxScale))
            
            matrix.setScale(scale, scale)
            matrix.postTranslate(
                (width - scale * drawable.intrinsicWidth) / 2,
                (height - scale * drawable.intrinsicHeight) / 2
            )
            imageMatrix = matrix
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (drawable != null) {
            matrix.setScale(scale, scale)
            matrix.postTranslate(
                (width - scale * drawable.intrinsicWidth) / 2,
                (height - scale * drawable.intrinsicHeight) / 2
            )
            imageMatrix = matrix
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                prevX = event.x
                prevY = event.y
                activePointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                if (scale > minScale) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    if (pointerIndex != -1) {
                        val currX = event.getX(pointerIndex)
                        val currY = event.getY(pointerIndex)
                        if (!scaleDetector.isInProgress) {
                            matrix.postTranslate(currX - prevX, currY - prevY)
                            imageMatrix = matrix
                            prevX = currX
                            prevY = currY
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    prevX = event.getX(newPointerIndex)
                    prevY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
} 