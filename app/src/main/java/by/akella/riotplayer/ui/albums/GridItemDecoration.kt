package by.akella.riotplayer.ui.albums

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(OFFSET, OFFSET, OFFSET, OFFSET)
    }

    companion object {
        private const val OFFSET = 24
    }
}
