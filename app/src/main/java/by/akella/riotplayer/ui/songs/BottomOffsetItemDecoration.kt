package by.akella.riotplayer.ui.songs

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import by.akella.riotplayer.R

class BottomOffsetItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val currItemPosition = parent.getChildAdapterPosition(view)
        val lastPosition = parent.adapter?.itemCount ?: 0
        if (currItemPosition == lastPosition) {
            outRect.bottom = view.context.resources.getDimensionPixelOffset(R.dimen.size_8)
        }
    }
}
