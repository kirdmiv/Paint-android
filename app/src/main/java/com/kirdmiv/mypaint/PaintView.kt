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
import java.lang.Float.max
import java.lang.Float.min

class PaintView(context: Context, atr: AttributeSet): View(context, atr) {
    private var paint = Paint()
    private var savedPaint = Paint()
    private var path = Path()
    private val paths: MutableList<Pair<Path, Paint>> = mutableListOf()
    private val deletedPaths: MutableList<Pair<Path, Paint>> = mutableListOf()
    private var startX = 0f
    private var startY = 0f
    var paintingMode: Int = 0

    init {
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = -1202
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f

        savedPaint.isAntiAlias = true
        savedPaint.isDither = true
        savedPaint.color = -1202
        savedPaint.style = Paint.Style.STROKE
        savedPaint.strokeJoin = Paint.Join.ROUND
        savedPaint.strokeCap = Paint.Cap.ROUND
        savedPaint.strokeWidth = 10f
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
                startX = x
                startY = y
                if (paintingMode == 0) {
                    path.moveTo(x, y)
                    path.lineTo(x, y)
                }
                postInvalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                when (paintingMode) {
                    1 -> drawRect(x, y)
                    2 -> drawOval(x, y)
                    3 -> drawLine(x, y)
                    else -> drawDefault(x, y)
                }
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
        savedPaint.color = color
        paint = Paint()
        paint.isAntiAlias = savedPaint.isAntiAlias
        paint.isDither = savedPaint.isDither
        paint.color = savedPaint.color
        paint.style = savedPaint.style
        paint.strokeJoin = savedPaint.strokeJoin
        paint.strokeCap = savedPaint.strokeCap
        paint.strokeWidth = savedPaint.strokeWidth

        Log.d("PaintView.kt -- setColor", "color : $color")
    }

    fun setThickness(thickness: Int) {
        savedPaint.strokeWidth = thickness.toFloat()
        paint = Paint()
        paint.isAntiAlias = savedPaint.isAntiAlias
        paint.isDither = savedPaint.isDither
        paint.color = savedPaint.color
        paint.style = savedPaint.style
        paint.strokeJoin = savedPaint.strokeJoin
        paint.strokeCap = savedPaint.strokeCap
        paint.strokeWidth = savedPaint.strokeWidth

        Log.d("PaintView.kt -- setThickness", "thickness: $thickness");
    }

    fun undo(){
        if (paths.isNotEmpty())
            deletedPaths.add(paths.removeAt(paths.lastIndex))
        postInvalidate()
    }

    fun redo() {
        if (deletedPaths.isNotEmpty())
            paths.add(deletedPaths.removeAt(deletedPaths.lastIndex))
        postInvalidate()
    }

    private fun drawRect(x: Float, y: Float){
        path.reset()
        path.addRect(
            min(startX, x),
            min(startY, y),
            max(startX, x),
            max(startY, y),
            Path.Direction.CCW
        )
    }

    private fun drawOval(x: Float, y: Float){
        path.reset()
        path.addOval(
            min(startX, x),
            min(startY, y),
            max(startX, x),
            max(startY, y),
            Path.Direction.CCW
        )
    }

    private fun drawLine(x: Float, y: Float){
        path.reset()
        path.moveTo(startX, startY)
        path.lineTo(x, y)
    }

    private fun drawDefault(x: Float, y: Float){
        path.lineTo(x, y)
        path.moveTo(x, y)
    }
}