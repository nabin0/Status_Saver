package com.zoomcodez.statussaver_downloadvideo.models

import android.net.Uri

data class WhatsappStatusModel(
    val name: String,
    val path: String,
    val uri: Uri,
    val fileName: String
)
