package by.akella.riotplayer.ui.albumdetails

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import by.akella.riotplayer.util.DescendantOffsetUtils
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.visible
import com.google.android.material.appbar.AppBarLayout
import by.akella.riotplayer.util.error as error1

class AlbumDetailsBehavior @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : CoordinatorLayout.Behavior<LinearLayout>(context, attributeSet) {

    private var tmpRect: Rect? = null
    private var currentAnimator: Animator? = null
    private var animState: Int = ANIM_STATE_NONE

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
            hide(child)
        } else {
            show(child)
        }

        return true
    }

    private fun show(child: LinearLayout) {
        if (isOrWillBeShown(child)) return

        if (ViewCompat.isLaidOut(child)) {
            val animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, 0f)
            animator.setAutoCancel(true)
            animator.addListener(object : AnimatorListenerAdapter() {
                private var isCancelled = false

                override fun onAnimationStart(animation: Animator?) {
                    child.visible()

                    currentAnimator = animation
                    animState = ANIM_STATE_SHOWING
                    isCancelled = false
                }

                override fun onAnimationEnd(animation: Animator?) {
                    currentAnimator = null
                    animState = ANIM_STATE_NONE
                }
            })
            animator.start()
        }
    }

    private fun hide(child: LinearLayout) {
        if (isOrWillBeHidden(child)) return

        if (ViewCompat.isLaidOut(child)) {
            val height = child.height.toFloat()

            val animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, -height)
            animator.setAutoCancel(true)
            animator.addListener(object : AnimatorListenerAdapter() {
                private var isCancelled = false

                override fun onAnimationStart(animation: Animator?) {
                    child.visible()

                    currentAnimator = animation
                    animState = ANIM_STATE_HIDING
                    isCancelled = false
                }

                override fun onAnimationEnd(animation: Animator?) {
                    currentAnimator = null
                    animState = ANIM_STATE_NONE

                    if (!isCancelled) {
                        child.gone()
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                    currentAnimator = null
                    animState = ANIM_STATE_NONE
                    isCancelled = true
                }
            })
            animator.start()
        }
    }

    private fun isValidDependency(dependency: View, child: View): Boolean {
        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        return lp.anchorId == dependency.id
    }

    private fun isOrWillBeShown(view: View): Boolean {
        return if (view.visibility != View.VISIBLE) {
            animState == ANIM_STATE_SHOWING
        } else {
            animState != ANIM_STATE_HIDING
        }
    }

    private fun isOrWillBeHidden(view: View): Boolean {
        return if (view.visibility == View.VISIBLE) {
            animState == ANIM_STATE_HIDING
        } else {
            animState != ANIM_STATE_SHOWING
        }
    }

    companion object {
        const val ANIM_STATE_NONE = 0
        const val ANIM_STATE_HIDING = 1
        const val ANIM_STATE_SHOWING: Int = 2
    }
}
