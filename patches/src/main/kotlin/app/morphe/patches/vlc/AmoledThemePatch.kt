package app.morphe.patches.vlc

import app.morphe.patches.vlc.Constants.COMPATIBILITY_VLC
import app.morphe.patcher.patch.resourcePatch

private val DARK_HEX = listOf(
    "#131313", "#212121", "#2a2a2a", "#323232", "#424242", "#121212", "#011422",
    "#ff303030", "#ff424242", "#ff212121",
    "#ff0f0d13", "#ff141218", "#ff1d1b20", "#ff211f26", "#ff2b2930",
    "#ff322f35", "#ff36343b", "#ff3b383e", "#ff48464c", "#ff605d64",
    "#ff1d1a22", "#ff322f37", "#ff49454f", "#ff605d66",
)

private val BLACK_THEME_TAG =
    "<style name=\"Theme.VLC.Apearance.Black\" parent=\"@style/Theme.MaterialComponents.NoActionBar\">"

@Suppress("unused")
val amoledBlackThemePatch = resourcePatch(
    name = "AMOLED black theme",
    description = "Repaints every dark surface — background, cards, dialogs, popups, header and the navigation bar — to true black.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VLC)

    execute {
        val colors = get("res/values/colors.xml")
        var text = colors.readText()
        for (hex in DARK_HEX) {
            text = text.replace(hex, "#000000")
        }
        text = text.replace("#ee2a2a2a", "#ee000000")
        colors.writeText(text)

        val styles = get("res/values/styles.xml")
        val override = BLACK_THEME_TAG + "\n" +
            "        <item name=\"elevationOverlayEnabled\">false</item>"
        styles.writeText(styles.readText().replace(BLACK_THEME_TAG, override))
    }
}
