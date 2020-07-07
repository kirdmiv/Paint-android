package com.kirdmiv.mypaint

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.lang.Float.max
import java.lang.Float.min
import kotlin.concurrent.thread


class PaintView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var paint = Paint()
    private var savedPaint = Paint()
    private var path = MyPath()
    private val paths: MutableList<Pair<MyPath, Paint>> = mutableListOf()
    private val deletedPaths: MutableList<Pair<MyPath, Paint>> = mutableListOf()
    private var startX = 0f
    private var startY = 0f
    private lateinit var picture: Bitmap
    var paintingMode: Int = 0

    init {
        savedPaint.isAntiAlias = true
        savedPaint.isDither = true
        savedPaint.color = 0
        savedPaint.style = Paint.Style.STROKE
        savedPaint.strokeJoin = Paint.Join.ROUND
        savedPaint.strokeCap = Paint.Cap.ROUND
        savedPaint.strokeWidth = 10f

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PaintView,
            0, 0
        ).apply {

            try {
                savedPaint.color = getColor(R.styleable.PaintView_defaultColor, 0)
                savedPaint.strokeWidth = getFloat(R.styleable.PaintView_defaultThickness, 10f)
            } finally {
                recycle()
            }
        }

        applyPaint()
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return

        picture = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
        val myCanvas = Canvas(picture)
        for (state in paths)
            myCanvas.drawPath(state.first, state.second)

        myCanvas.drawPath(path, paint)
        canvas.drawBitmap(picture, 0f, 0f, null)
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
                // saveImage()
                Log.d("PaintView.kt -- ACTION_UP", path.toString())
                paths.add(Pair(path, paint))
                path = MyPath()
                Log.d("PaintView.kt -- ACTION_UP", "new path")
            }
        }
        postInvalidate()
        return false
    }

    fun setColor(color: Int) {
        savedPaint.color = color
        applyPaint()

        Log.d("PaintView.kt -- setColor", "color : $color")
    }

    fun setThickness(thickness: Int) {
        savedPaint.strokeWidth = thickness.toFloat()
        applyPaint()

        Log.d("PaintView.kt -- setThickness", "thickness: $thickness");
    }

    fun undo() {
        if (paths.isNotEmpty())
            deletedPaths.add(paths.removeAt(paths.lastIndex))
        postInvalidate()
    }

    fun redo() {
        if (deletedPaths.isNotEmpty())
            paths.add(deletedPaths.removeAt(deletedPaths.lastIndex))
        postInvalidate()
    }

    private fun drawRect(x: Float, y: Float) {
        path.reset()
        path.addRect(
            min(startX, x),
            min(startY, y),
            max(startX, x),
            max(startY, y),
            Path.Direction.CCW
        )
    }

    private fun drawOval(x: Float, y: Float) {
        path.reset()
        path.addOval(
            min(startX, x),
            min(startY, y),
            max(startX, x),
            max(startY, y),
            Path.Direction.CCW
        )
    }

    private fun drawLine(x: Float, y: Float) {
        path.reset()
        path.moveTo(startX, startY)
        path.lineTo(x, y)
    }

    private fun drawDefault(x: Float, y: Float) {
        path.lineTo(x, y)
        //path.moveTo(x, y)
    }

    //rotate current path by degrees via computing center
    private fun rotate(degrees: Float) {
        val matrix = Matrix()
        val bounds = RectF();
        path.computeBounds(bounds, true);
        matrix.postRotate(degrees, bounds.centerX(), bounds.centerY())
        path.transform(matrix)
    }

    private fun applyPaint() {
        paint = Paint()
        paint.isAntiAlias = savedPaint.isAntiAlias
        paint.isDither = savedPaint.isDither
        paint.color = savedPaint.color
        paint.style = savedPaint.style
        paint.strokeJoin = savedPaint.strokeJoin
        paint.strokeCap = savedPaint.strokeCap
        paint.strokeWidth = savedPaint.strokeWidth
    }

    fun clear() {
        path.reset()
        paths.clear()
        deletedPaths.clear()
        postInvalidate()
    }

    fun saveImage() {
        try {
            val filename: String =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
            val f = File(filename, "myImage.png")
            f.createNewFile()
            println("file created $f")
            val out = FileOutputStream(f)
            thread {
                picture.compress(Bitmap.CompressFormat.PNG, 90, out)
                out.flush()
                out.close()
                MediaScannerConnection.scanFile(context,
                    arrayOf(f.absolutePath), null, null);
                println("compressed???")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyVectorPath(){
        var vectorPath: String = ""
        for (state in paths)
            vectorPath += state.first.toString()
        vectorPath += path.toString()
        Log.d("PaintView.kt -- copyvectorPath()", vectorPath)
        val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("vector path", vectorPath)
        clipBoard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            "Vector path of your drawing copied to clipboard",
            Toast.LENGTH_SHORT
        ).show()
    }
}