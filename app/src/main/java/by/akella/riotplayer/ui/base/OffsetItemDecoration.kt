package by.akella.riotplayer.ui.base

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class OffsetItemDecoration(
    private val start: Int = 0,
    private val top: Int = 0,
    private val end: Int = 0,
    private val bottom: Int = 0,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = start
        outRect.right = end

        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = top
        }

        if (parent.getChildAdapterPosition(view) == parent.childCount - 1) {
            outRect.bottom = bottom
        }
    }
}