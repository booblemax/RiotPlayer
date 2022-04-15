package by.akella.riotplayer.ui.custom

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import by.akella.riotplayer.R
import kotlin.math.ceil

class MusicProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarStyle
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    var onTouchEnds: (Long) -> Unit = {}

    var valueFrom: Long = -1
        set(value) {
            if (value > valueTo) throw IllegalArgumentException("valueFrom must be less than valueTo")
            field = value
        }

    var valueTo: Long = 0

    var value: Long = 0
        set(value) {
            if (!isTrackingStarted) {
                progress = ceil(value * DIVIDEND / interval).toInt()
            }
            field = value
        }
        get() = ceil(progress * interval / DIVIDEND).toLong()

    private var isTrackingStarted = false
    private val interval: Long get() = valueTo - valueFrom

    init {
        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //nothing to impl
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTrackingStarted = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTrackingStarted = false
                onTouchEnds(value)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    fun disableTouch() {
        setOnTouchListener { _, _ -> true }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun enableTouch() {
        setOnTouchListener { _, e -> super.onTouchEvent(e) }
    }

    companion object {
        private const val DIVIDEND = 100.0
    }
}
