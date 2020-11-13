package by.akella.riotplayer.ui.custom

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.res.ResourcesCompat
import by.akella.riotplayer.R
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

open class CircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defAttrs: Int = 0
) : View(context, attrs, defAttrs) {

    private var animator: ValueAnimator? = null
    private var needAnimate: Boolean = false

    private var centerX: Float = 0f
    private var centerY: Float = 0f

    private var thumbX = 0f
    private var thumbY = 0f

    private var angleProgressArcInDegree = 0f
    private var progress = MIN_PROGRESS

    var onTouchEnds: (Long) -> Unit = {}

    var valueFrom: Float = -1f
        set(value) {
            if (value > valueTo) throw IllegalArgumentException("valueFrom must be less than valueTo")
            field = value
        }

    var valueTo: Float = 0f

    var value: Float = 0f
        set(value) {
            updateProgressValue(value)
            field = value
        }

    private val pathWidth = resources.getDimension(R.dimen.path_width)
    private val thumbRadius = resources.getDimension(R.dimen.thumb_radius)
    private val interval: Float get() = valueTo - valueFrom

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

    private var radius: Float = 0f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = max(width, height)
        radius = min / 2f - getAdditionalOffset()
        centerX = (width - paddingStart - paddingEnd) / 2f
        centerY = (height - paddingStart - paddingEnd) / 2f

        setMeasuredDimension(min, min)
        updateThumbPosition(progress)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawCircle(centerX, centerY, radius, pathPaint)
        canvas?.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)

        val start = paddingStart + getAdditionalOffset()
        val top = paddingTop + getAdditionalOffset()
        val end = width.toFloat() - getAdditionalOffset()
        val bottom = height.toFloat() - getAdditionalOffset()

        canvas?.drawArc(
            start, top, end, bottom,
            -90f, angleProgressArcInDegree, false, linePaint
        )
    }

    private fun animateProgressChanging(oldProgress: Float, newProgress: Float) {
        if (animator?.isStarted == true) animator?.cancel()

        animator = ValueAnimator.ofFloat(oldProgress, newProgress).apply {
            duration = ANIM_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val currProgressValue = it.animatedValue as Float
                updatePositions(currProgressValue)
            }
        }
        animator?.start()
    }

    private fun updatePositions(currProgress: Float) {
        updateThumbPosition(currProgress)
        updateProgressPath(currProgress)

        invalidate()
    }

    private fun updateThumbPosition(currProgress: Float) {
        val rads = calculateProgressAngleInRadian(currProgress)
        thumbX = (measuredWidth / 2 + radius * cos(rads)).toFloat()
        thumbY = (measuredHeight / 2 + radius * sin(rads)).toFloat()
    }

    private fun calculateProgressAngleInRadian(progress: Float): Double {
        val angle = progress / DIVIDEND * FULL_ANGLE - 90
        return angle * Math.PI / HALF_ANGLE
    }

    private fun updateProgressPath(currProgress: Float) {
        angleProgressArcInDegree = currProgress / DIVIDEND * FULL_ANGLE
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)

        val x = event.x
        val y = event.y

        return when {
            (event.action == MotionEvent.ACTION_MOVE ||
                    event.action == MotionEvent.ACTION_DOWN) && isTouchOnPathRegion(x, y) -> {
                updateCurrentValue(x, y)
                needAnimate = event.action == MotionEvent.ACTION_DOWN
                true
            }
            event.action == MotionEvent.ACTION_UP && isTouchOnPathRegion(x, y) -> {
                onTouchEnds(value.toLong())
                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private fun updateCurrentValue(x: Float, y: Float) {
        val cos = computeCos(x, y)
        val angle =
            if (x < width / 2) Math.PI * RADIAN + acos(cos) * RADIAN
            else Math.PI * RADIAN - acos(cos) * RADIAN
        value = (valueTo * angle / 360).toFloat()
    }

    private fun isTouchOnPathRegion(x: Float, y: Float): Boolean {
        val outRadius = radius + pathWidth + thumbRadius
        val inRadius = radius - pathWidth - thumbRadius
        val powCathetuses = (centerX - x).pow(2) + (centerY - y).pow(2)
        return inRadius * inRadius <= powCathetuses && powCathetuses <= outRadius * outRadius
    }

    private fun computeCos(x: Float, y: Float): Float {
        val w = x - width / 2
        val h = y - height / 2
        val slope = sqrt(w * w + h * h)
        return h / slope
    }

    private fun getAdditionalOffset() = max(pathWidth, thumbRadius)

    private fun updateProgressValue(value: Float) {
        val oldProgress = progress
        val newProgress = value * DIVIDEND / interval

        progress = if (newProgress >= MAX_PROGRESS) {
            MAX_PROGRESS
        } else {
            newProgress
        }

        if (needAnimate) animateProgressChanging(oldProgress, progress)
        else {
            if (animator?.isStarted == true) animator?.cancel()
            updatePositions(progress)
        }
    }

    companion object {
        private const val DIVIDEND = 100.0f
        private const val FULL_ANGLE = 360f
        private const val HALF_ANGLE = 180f
        private const val RADIAN = 180f / Math.PI

        private const val ANIM_DURATION = 300L
        private const val MAX_PROGRESS = 100f
        private const val MIN_PROGRESS = 0f
    }
}