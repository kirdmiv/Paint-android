package com.kirdmiv.mypaint.paintview

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.net.toFile
import com.kirdmiv.mypaint.R
import java.io.File
import java.io.IOException
import java.lang.Float.max
import java.lang.Float.min
import java.util.*


class PaintView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var paint = Paint()
    private var savedPaint = Paint()
    private var path = MyPath()
    private val paths: MutableList<Pair<MyPath, Paint>> = mutableListOf()
    private val deletedPaths: MutableList<Pair<MyPath, Paint>> = mutableListOf()
    private var startX = 0f
    private var startY = 0f
    private var canvasWidth = 0
    private var canvasHeight = 0
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
        canvasHeight = canvas.height
        canvasWidth = canvas.width
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


    @Suppress("NAME_SHADOWING")
    fun saveImage() {
        val relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + "Paint"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            uri?.let { uri ->
                val stream = resolver.openOutputStream(uri)

                stream?.let { stream ->
                    if (!picture.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                        throw IOException("Failed to save bitmap.")
                    } else {
                        val file = File(uri.path!!)
                        Toast.makeText(
                            context,
                            "Image saved to ${uri.path}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } ?: throw IOException("Failed to get output stream.")

            } ?: throw IOException("Failed to create new MediaStore record")

        } catch (e: IOException) {
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
            throw IOException(e)
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
    }

    fun copyVectorPath() {
        var vectorPath: String = ""
        for (state in paths)
            vectorPath += state.first.toString()
        vectorPath += path.toString()
        Log.d("PaintView.kt -- copyVectorPath()", vectorPath)
        val clipBoard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("vector path", vectorPath)
        clipBoard.setPrimaryClip(clip)
        Toast.makeText(
            context,
            "Vector path of your drawing copied to clipboard",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun collectSvg(): String {
        var ans: String = "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.0//EN\"\n" +
                " \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">\n" +
                "<svg version=\"1.0\" xmlns=\"http://www.w3.org/2000/svg\"\n" +
                " width=\"${canvasWidth}pt\" height=\"${canvasHeight}pt\" viewBox=\"0 0 $canvasWidth $canvasHeight\"\n" +
                " preserveAspectRatio=\"xMidYMid meet\">\n" +
                "<metadata>\n" +
                "Created by MyPaint 1.0, written by Kirill Ivanov 2020\n" +
                "</metadata>\n"

        for (state in paths)
            ans += "<path d=\"${state.first}\" fill=\"none\" " +
                    "stroke=\"#${Integer.toHexString(state.second.color)
                        .toUpperCase(Locale.getDefault()).substring(2)}\" stroke-width=\"${state.second.strokeWidth}\"/>\n"

        ans += "</svg>\n"
        return ans
    }
}