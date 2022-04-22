@file:Suppress("TooManyFunctions")
package by.akella.riotplayer.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.akella.riotplayer.ui.custom.SafeClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost

fun Activity.setupToolbar(toolbar: Toolbar) {
    (this as AppCompatActivity).setSupportActionBar(toolbar)
}

inline fun View.snack(
    message: String,
    length: Int = Snackbar.LENGTH_LONG,
    f: Snackbar.() -> Unit = {}
) {
    val snack = Snackbar.make(this, message, length)
    snack.f()
    snack.show()
}

fun View.visible() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.animateVisible() {
    animate().alpha(1f).start()
}

fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun View.animateGone() {
    animate().alpha(0f).start()
}

fun <T : Any?> View.onSafeClick(listener: SafeClickListener<T>, item: T? = null) {
    setOnClickListener { listener.invoke(item) }
}

fun Fragment.waitForTransition(targetView: View) {
    postponeEnterTransition()
    targetView.doOnPreDraw { startPostponedEnterTransition() }
}

fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY

fun AppCompatImageView.loadAlbumIcon(
    albumIconPath: String,
    @DrawableRes default: Int,
    action: () -> Unit = {}
) {
    Glide.with(this)
        .asBitmap()
        .load(albumIconPath)
        .error(default)
        .addListener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                action()
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                action()
                return false
            }
        })
        .into(this)
}

fun AppCompatImageView.loadAlbumIconCircle(
    albumIconPath: String,
    @DrawableRes default: Int,
    action: () -> Unit = {}
) {
    Glide.with(this)
        .load(albumIconPath)
        .error(default)
        .optionalCircleCrop()
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                action()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                action()
                return false
            }
        })
        .into(this)
}

fun <STATE : Any, SIDE_EFFECT : Any> Container<STATE, SIDE_EFFECT>.collectState(
    lifecycleOwner: LifecycleOwner,
    block: suspend (state: STATE) -> Unit
) {
    with(lifecycleOwner) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                stateFlow
                    .onEach(block)
                    .launchIn(this)
            }
        }
    }
}

fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.collectState(
    lifecycleOwner: LifecycleOwner,
    block: suspend (state: STATE) -> Unit
) {
    with(lifecycleOwner) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                container.stateFlow
                    .onEach(block)
                    .launchIn(this)
            }
        }
    }
}
