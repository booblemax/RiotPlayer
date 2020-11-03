package by.akella.riotplayer.util

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import by.akella.riotplayer.R
import by.akella.riotplayer.ui.custom.SafeClickListener
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

fun Activity.setupToolbar(toolbar: Toolbar) {
    (this as AppCompatActivity).setSupportActionBar(toolbar)
}

inline fun View.snack(
    @StringRes messageRes: Int,
    length: Int = Snackbar.LENGTH_LONG,
    f: Snackbar.() -> Unit = {}
) {
    snack(resources.getString(messageRes), length, f)
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

fun View.invisible() {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }
}

fun View.gone() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun View.animateGone() {
    animate().alpha(0f).start()
}

fun <T : Any?> View.onSafeClick(item: T? = null, listener: SafeClickListener<T>) {
    setOnClickListener { listener.invoke(item) }
}

fun Fragment.waitForTransition(targetView: View) {
    postponeEnterTransition()
    targetView.doOnPreDraw { startPostponedEnterTransition() }
}

fun String?.toUri(): Uri = this?.let { Uri.parse(it) } ?: Uri.EMPTY

fun AppCompatImageView.loadAlbumIcon(albumIconPath: String, @DrawableRes default: Int) {
    Glide.with(this)
        .asBitmap()
        .load(albumIconPath)
        .error(default)
        .into(this)
}
