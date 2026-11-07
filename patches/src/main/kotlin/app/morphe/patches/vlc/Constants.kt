package app.morphe.patches.vlc

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_VLC = Compatibility(
        name = "VLC",
        packageName = "org.videolan.vlc",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF6600,
        targets = listOf(
            AppTarget(
                version = null,
                isExperimental = true
            ),
            AppTarget(
                version = "3.7.1"
            ),
            AppTarget(
                version = "3.7.2"
            )
        )
    )
}
