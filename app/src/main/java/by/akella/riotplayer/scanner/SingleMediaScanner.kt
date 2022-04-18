package by.akella.riotplayer.scanner

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment

class SingleMediaScanner(
    private val context: Context,
) : MediaScannerConnection.MediaScannerConnectionClient {

    private val scanner = MediaScannerConnection(context, this)
    var onScanComplete: () -> Unit = { }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        scanner.disconnect()
        onScanComplete()
    }

    override fun onMediaScannerConnected() {
        scanner.scanFile("files://sdcard/", null)
    }

    fun scan() {
        scanner.connect()
    }
}
