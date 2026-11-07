package app.morphe.patches.vlc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

object WhatsNewLaunchFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/util/WhatsNewManager;",
    name = "launchIfNeeded",
    returnType = "V",
    parameters = listOf("Landroidx/appcompat/app/AppCompatActivity;"),
)

object FeatureFlagEnabledFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/vlc/util/FeatureFlagManager;",
    name = "isEnabled",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Lorg/videolan/vlc/util/FeatureFlag;"),
)

object AudioDigitalOutputFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/resources/VLCOptions;",
    name = "isAudioDigitalOutputEnabled",
    returnType = "Z",
    parameters = listOf("Landroid/content/SharedPreferences;"),
)

object AoutFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/resources/VLCOptions;",
    name = "getAout",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Landroid/content/SharedPreferences;"),
)

object LibOptionsFingerprint : Fingerprint(
    definingClass = "Lorg/videolan/resources/VLCOptions;",
    name = "getLibOptions",
    returnType = "Ljava/util/ArrayList;",
    parameters = listOf(),
    filters = listOf(
        opcode(Opcode.RETURN_OBJECT)
    ),
)
