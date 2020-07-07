package com.kirdmiv.mypaint

class PathEvent(var action: Char, var x: Float, var y: Float) {
    override fun toString(): String {
        when (action) {
            'L', 'M' -> return "${action}${x},${y}"
            'H' -> return "${action}${x}"
            'V' -> return "${action}${y}"
        }
        return ""
    }
}