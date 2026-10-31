package app.morphe.patches.crunchyroll.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    val COMPATIBILITY_CRUNCHYROLL = Compatibility(
        name = "Crunchyroll",
        packageName = "com.crunchyroll.crunchyroid",
        targets = listOf(
            AppTarget("3.126.0"),
            AppTarget("3.117.0"),
        ),
    )
}
