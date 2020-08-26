package io.github.mattpvaughn.ps5launcher

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppModel constructor(val drawable: Drawable?, val appName: String, val packageName: String, val launchIntent: Intent?)
