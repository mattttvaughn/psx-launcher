package io.github.mattpvaughn.ps5launcher

import android.content.Context
import android.content.Intent
import android.content.pm.ComponentInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

/**
 * A representation of an application as a simple data class. Contains information needed to
 * uniquely identify the app as well as launcher it
 *
 * TODO: [launchIntent] could probably just be replaced with a string representation of [ComponentInfo],
 *       which might matter for persistence. Something will have to happen with [drawable] too
 */
data class AppModel constructor(
    val drawable: Drawable?,
    val appName: String,
    val packageName: String,
    val launchIntent: Intent?
)
