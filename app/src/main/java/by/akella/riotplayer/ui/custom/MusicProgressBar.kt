package by.akella.riotplayer.ui.custom

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider

class MusicProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Slider(context, attrs, defStyleAttr) {

    var onTouchEnds: (Int) -> Unit = {}

    private var isTrackingStarted = false

    init {
        labelBehavior = LabelFormatter.LABEL_GONE
        addOnSliderTouchListener(object : OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isTrackingStarted = true
            }

            override fun onStopTrackingTouch(slider: Slider) {
                isTrackingStarted = false
                onTouchEnds(value.toInt())
            }
        })
    }

    override fun setValue(value: Float) {
        if (!isTrackingStarted) {
            super.setValue(value)
        }
    }
}
