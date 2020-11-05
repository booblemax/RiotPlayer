package by.akella.riotplayer.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import by.akella.riotplayer.R
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class CircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defAttrs: Int = 0
) : View(context, attrs, defAttrs) {

    private var centerX: Float = 0f
    private var centerY: Float = 0f

    private var thumbX = 0f
    private var thumbY = 0f

    private var progress = 0f

    var valueFrom: Long = 0 // todo reset to -1
        set(value) {
            if (value > valueTo) throw IllegalArgumentException("valueFrom must be less than valueTo")
            field = value
        }

    var valueTo: Long = 100 // todo reset to 0

    var value: Long = 0
        set(value) {
            progress = ceil(value * DIVIDEND / interval).toFloat()
            updateThumbPosition()
            field = value
            invalidate()
        }
        get() = ceil(progress * interval / DIVIDEND).toLong()

    private val pathWidth = resources.getDimension(R.dimen.path_width)
    private val thumbRadius = resources.getDimension(R.dimen.thumb_radius)
    private val interval: Long get() = valueTo - valueFrom

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.pink_light, context.theme)
        style = Paint.Style.STROKE
        strokeWidth = pathWidth
        strokeCap = Paint.Cap.ROUND
    }
    private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.pink, context.theme)
        style = Paint.Style.STROKE
        strokeWidth = pathWidth
    }

    private val thumbPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ResourcesCompat.getColor(resources, R.color.pink, context.theme)
        style = Paint.Style.FILL
    }

    private var r: Float = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = max(width, height)
        r = min / 2f - pathWidth
        centerX = (width - paddingStart - paddingEnd) / 2f
        centerY = (height - paddingStart - paddingEnd) / 2f

        updateThumbPosition()
        setMeasuredDimension(min, min)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(centerX, centerY, r, pathPaint)
        canvas?.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)

        val start = paddingStart + pathWidth
        val top = paddingTop + pathWidth
        val end = width.toFloat() - pathWidth
        val bottom = height.toFloat() - pathWidth
        canvas?.drawArc(
            start, top, end, bottom,
            -90f, (progress / DIVIDEND * FULL_ANGLE).toFloat(), false, linePaint)
    }

    private fun updateThumbPosition() {
        val angle = progress / DIVIDEND * FULL_ANGLE - 90
        val rads = angle * Math.PI / HALF_ANGLE
        thumbX = (measuredWidth / 2 + r * cos(rads)).toFloat()
        thumbY = (measuredHeight / 2 + r * sin(rads)).toFloat()
    }

    companion object {
        private const val DIVIDEND = 100.0
        private const val FULL_ANGLE = 360f
        private const val HALF_ANGLE = 180f
    }
}