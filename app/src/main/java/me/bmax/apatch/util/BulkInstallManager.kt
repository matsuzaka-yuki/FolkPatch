package me.bmax.apatch.util

import android.net.Uri

object BulkInstallManager {
    private val queue = ArrayList<Uri>()

    fun setQueue(uris: List<Uri>) {
        queue.clear()
        queue.addAll(uris)
    }

    fun hasNext(): Boolean {
        return queue.isNotEmpty()
    }

    fun popNext(): Uri? {
        return if (queue.isNotEmpty()) {
            queue.removeAt(0)
        } else {
            null
        }
    }

    fun clear() {
        queue.clear()
    }
}
