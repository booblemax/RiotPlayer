package by.akella.riotplayer.ui.albumdetails

import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class DetailsItemDecoration(@DimenRes private val topOffset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.getChildLayoutPosition(view) == 0) {
            val offset = parent.context.resources.getDimensionPixelOffset(topOffset)
            outRect.top = offset
        }
    }
}
