package by.akella.riotplayer.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar
import by.akella.riotplayer.R
import kotlin.math.ceil
import kotlin.math.floor

class MusicProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.seekBarStyle
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    var onTouchEnds: (Int) -> Unit = {}

    var valueFrom: Int = -1
        set(value) {
            if (value > valueTo) throw IllegalArgumentException("valueFrom must be less than valueTo")
            field = value
        }

    var valueTo: Int = 0

    var value: Int = 0
        set(value) {
            if (!isTrackingStarted) {
                progress = ceil(value * DIVIDEND / getInterval()).toInt()
            }
            field = value
        }
        get() = ceil(progress * getInterval() / DIVIDEND).toInt()

    private var isTrackingStarted = false

    init {
        setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTrackingStarted = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isTrackingStarted = false
                onTouchEnds(value)
            }
        })
    }

    private fun getInterval(): Int = valueTo - valueFrom

    fun disableTouch() {
        setOnTouchListener { _, _ -> true }
    }

    companion object {
        private const val DIVIDEND = 100.0
    }
}
