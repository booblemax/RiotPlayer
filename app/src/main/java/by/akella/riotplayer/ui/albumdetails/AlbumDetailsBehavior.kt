package by.akella.riotplayer.ui.albumdetails

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import by.akella.riotplayer.util.DescendantOffsetUtils
import com.google.android.material.appbar.AppBarLayout

class AlbumDetailsBehavior @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : CoordinatorLayout.Behavior<LinearLayout>(context, attributeSet) {

    private var tmpRect: Rect? = null
    private val animationHelper = AlbumDetailsAnimationHelper()

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: LinearLayout,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: LinearLayout,
        dependency: View
    ): Boolean {
        updateVisibility(parent, child, dependency as AppBarLayout)
        return true
    }

    override fun onLayoutChild(
        parent: CoordinatorLayout,
        child: LinearLayout,
        layoutDirection: Int
    ): Boolean {
        parent.getDependencies(child).forEach { view ->
            if (view is AppBarLayout) {
                updateVisibility(parent, child, view)
            }
        }

        parent.onLayoutChild(child, layoutDirection)
        return true
    }

    private fun updateVisibility(
        parent: CoordinatorLayout,
        child: LinearLayout,
        dependency: AppBarLayout
    ): Boolean {
        if (!isValidDependency(dependency, child)) return false

        if (tmpRect == null) {
            tmpRect = Rect()
        }

        val rect: Rect = tmpRect!!
        DescendantOffsetUtils.getDescendantRect(parent, dependency, rect)

        if (rect.bottom <= dependency.minimumHeightForVisibleOverlappingContent) {
            animationHelper.hide(child)
        } else {
            animationHelper.show(child)
        }

        return true
    }

    private fun isValidDependency(dependency: View, child: View): Boolean {
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        return lp.anchorId == dependency.id
    }
}
