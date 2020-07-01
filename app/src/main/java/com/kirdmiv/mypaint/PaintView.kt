package com.kirdmiv.mypaint
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class PaintView(context: Context, atr: AttributeSet): View(context, atr) {
    private var paint = Paint()
    private var path = Path()
    private val paths: MutableList<Pair<Path, Paint>> = mutableListOf()
    private val deletedPaths: MutableList<Pair<Path, Paint>> = mutableListOf()

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

        for (state in paths)
            canvas.drawPath(state.first, state.second)

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
                paths.add(Pair(path, paint))
                path = Path()
                Log.d("PaintView.kt -- ACTION_UP", "new path")
            }
        }
        postInvalidate()
        return false
    }

    fun setColor(color: Int) {
        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
    }

    fun setThickness(thickness: Int) {
        paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = -1301
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = thickness.toFloat()
        Log.d("PaintView.kt -- setThickness", "thickness: $thickness");
    }

    fun undo(){
        if (paths.isNotEmpty())
            deletedPaths.add(paths.removeAt(paths.lastIndex))
    }

    fun redo() {
        if (deletedPaths.isNotEmpty())
            paths.add(deletedPaths.removeAt(deletedPaths.lastIndex))
    }
}