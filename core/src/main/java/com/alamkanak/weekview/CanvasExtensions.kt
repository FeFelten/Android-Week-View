package com.alamkanak.weekview

import android.graphics.Canvas

internal fun Canvas.withTranslation(x: Float, y: Float, block: Canvas.() -> Unit) {
    save()
    translate(x, y)
    block()
    restore()
}
