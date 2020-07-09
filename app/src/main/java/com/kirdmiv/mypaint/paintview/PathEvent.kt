package com.kirdmiv.mypaint.paintview

open class PathEvent(var action: Char, var x: Float, var y: Float) {
    override fun toString(): String {
        when (action) {
            'L', 'M' -> return "${action}${x},${y}"
            'H' -> return "${action}${x}"
            'V' -> return "${action}${y}"
        }
        return ""
    }
}

class AEevnt(
    private val rx: Float,
    private val ry: Float,
    private val xAxisRotation: Float,
    private val largeArcFlag: Boolean,
    private val sweepFlag: Boolean,
    x: Float,
    y: Float
) : PathEvent('a', x, y) {
    override fun toString(): String {
        return "$action$rx,$ry $xAxisRotation ${largeArcFlag.toInt()},${sweepFlag.toInt()} $x,$y"
    }

    private fun Boolean.toInt() : Int {
        if (this) return 1;
        return 0;
    }
}