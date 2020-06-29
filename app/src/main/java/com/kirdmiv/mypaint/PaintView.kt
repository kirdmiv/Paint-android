package com.kirdmiv.mypaint
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PaintView(context: Context, atr: AttributeSet): View(context, atr) {
    private val paint = Paint()
    private var path = Path()
    private val paths: MutableList<Path> = mutableListOf()

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = -1202
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        for (mPath in paths)
            canvas.drawPath(mPath, paint)

        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                path.lineTo(x, y)
                postInvalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                paths.add(path)
                path = Path()
            }
        }
        postInvalidate()
        return false
    }

    fun setColor(color: Int) {
        paint.color = color
    }
}