package com.zoomcodez.statussaver_downloadvideo

import android.os.Environment
import java.io.File

object Util {
    val RootDirectoryWhatsapp: File =
        File("${Environment.getExternalStorageDirectory()}/Documents/StatusSaver/Whatsapp")

    fun createFileFolder() {
        if (!RootDirectoryWhatsapp.exists()) {
            RootDirectoryWhatsapp.mkdirs()
        }
    }

    const val URI_TREE: String = "uri_tree_for_whatsapp"
    var DOWNLOAD_COUNT: Int = 0
}