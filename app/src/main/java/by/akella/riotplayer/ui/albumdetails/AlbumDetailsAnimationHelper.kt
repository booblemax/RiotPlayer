package by.akella.riotplayer.ui.albumdetails

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import by.akella.riotplayer.util.gone
import by.akella.riotplayer.util.visible

class AlbumDetailsAnimationHelper {

    private var currentAnimator: Animator? = null
    private var animState: Int = ANIM_STATE_NONE

    fun show(child: LinearLayout) {
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

    fun hide(child: LinearLayout) {
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
