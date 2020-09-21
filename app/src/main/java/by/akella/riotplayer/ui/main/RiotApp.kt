package by.akella.riotplayer.ui.main

import android.app.Application
import com.google.android.exoplayer2.ui.BuildConfig
import timber.log.Timber

class RiotApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}