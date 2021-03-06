package com.kirdmiv.mypaint.paintview

import android.graphics.Path

class MyPath() : Path() {
    private val points: MutableList<PathEvent> = mutableListOf()

    override fun lineTo(x: Float, y: Float) {
        super.lineTo(x, y)
        points.add(PathEvent('L', x, y))
    }

    override fun moveTo(x: Float, y: Float) {
        super.moveTo(x, y)
        points.add(PathEvent('M', x, y))
    }

    override fun addRect(left: Float, top: Float, right: Float, bottom: Float, dir: Direction) {
        super.addRect(left, top, right, bottom, dir)
        if (dir == Path.Direction.CCW){
            points.add(PathEvent('M', left, top))
            points.add(PathEvent('V', left, bottom))
            points.add(PathEvent('H', right, bottom))
            points.add(PathEvent('V', right, top))
            points.add(PathEvent('H', left, top))
        } else {
            points.add(PathEvent('M', left, top))
            points.add(PathEvent('H', left, top))
            points.add(PathEvent('V', right, top))
            points.add(PathEvent('H', right, bottom))
            points.add(PathEvent('V', left, bottom))
        }
    }

    override fun addOval(left: Float, top: Float, right: Float, bottom: Float, dir: Direction) {
        super.addOval(left, top, right, bottom, dir)
        points.add(PathEvent('M', left, (top + bottom) / 2f))

        points.add(AEevnt(
            (right - left) / 2f,
            (top - bottom) / 2f,
            0f,
            largeArcFlag = true,
            sweepFlag = false,
            x = right - left,
            y = 0f
        ))
        points.add(AEevnt(
            (right - left) / 2f,
            (top - bottom) / 2f,
            0f,
            largeArcFlag = true,
            sweepFlag = false,
            x = left - right,
            y = 0f
        ))
    }

    override fun toString(): String {
        var res: String = ""
        for (event in points){
            res += event.toString()
        }
        return res
    }

    override fun reset() {
        super.reset()
        points.clear()
    }
}