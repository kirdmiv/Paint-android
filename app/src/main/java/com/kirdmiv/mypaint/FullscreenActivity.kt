package com.kirdmiv.mypaint

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.kirdmiv.mypaint.animation.BounceInt
import java.lang.Integer.getInteger

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private lateinit var paint: PaintView
    private lateinit var paintControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        paint.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        paintControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true
        // Set up the user interaction to manually show or hide the system UI.
        paint = findViewById(R.id.paintV)
        paint.setOnClickListener { }
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { customize(); hide() }

        paintControls = findViewById(R.id.fullscreen_content_controls)

        hide()

        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(0)
    }

    override fun onStart() {
        super.onStart()
        hide()
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        paintControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        paint.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    private fun customize() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val bottomSheetView: View = LayoutInflater.from(applicationContext)
            .inflate(
                R.layout.dialog_customization,
                findViewById(R.id.bottom_sheet)
            )

        bottomSheetView.findViewById<ImageButton>(R.id.color_red)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.red))
                dismiss(dialog)
            }
        bottomSheetView.findViewById<ImageButton>(R.id.color_blue)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.blue))
                dismiss(dialog)
            }
        bottomSheetView.findViewById<ImageButton>(R.id.color_green)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.green))
                dismiss(dialog)
            }
        bottomSheetView.findViewById<ImageButton>(R.id.color_yellow)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.yellow))
                dismiss(dialog)
            }
        bottomSheetView.findViewById<ImageButton>(R.id.color_purple)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.purple))
                dismiss(dialog)
            }
        bottomSheetView.findViewById<ImageButton>(R.id.color_black)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.black))
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.undo_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.undo()
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.redo_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.redo()
                dismiss(dialog)
            }

        bottomSheetView.findViewById<SeekBar>(R.id.thick_sb)
            .setOnSeekBarChangeListener(MySeekBarListener(
                paint,
                resources.getInteger(R.integer.min_thickness),
                resources.getInteger(R.integer.max_thickness),
                resources.getInteger(R.integer.step)
                )
            )

        bottomSheetView.findViewById<ImageButton>(R.id.rect_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.paintingMode = 1
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.oval_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.paintingMode = 2
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.straight_line_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.paintingMode = 3
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.line_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.paintingMode = 0
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageView>(R.id.erase_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.setColor(getColor(R.color.white))
                //dialog.dismiss()
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.delete_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.clear()
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.save_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.saveImage()
                dismiss(dialog)
            }

        bottomSheetView.findViewById<ImageButton>(R.id.save_svg_btn)
            .setOnClickListener {
                tapAnimation(it)
                paint.copyVectorPath()
                dismiss(dialog)
            }

        dialog.setContentView(bottomSheetView)
        dialog.show()
    }

    class MySeekBarListener(paintView: PaintView, min_thick: Int, max_thick: Int, steps: Int): SeekBar.OnSeekBarChangeListener {
        private val minThickness = min_thick
        private val maxThickness = max_thick
        private val step = steps
        private val pv = paintView


        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //Log.d("MainActivity.kt -- onProgChanged", "step ${step.toString()}")
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
            p0?.progress?.let {
                    pv.setThickness(minThickness + (it * step))
            }
        }
    }

    private fun tapAnimation(v: View){
        val animation = AnimationUtils.loadAnimation(this, R.anim.bounce)
        val interpolator = BounceInt(0.2, 10.0)
        animation.interpolator = interpolator
        //v.animation = animation
        v.startAnimation(animation)
        v.invalidate()
        Log.d("MainActivity.kt -- tapAnimation", "ok?")
    }

    private fun dismiss(dialog: BottomSheetDialog){
        return
        dialog.dismiss()
    }
}