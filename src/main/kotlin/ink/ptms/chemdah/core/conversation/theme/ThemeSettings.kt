package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.core.conversation.Session
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XSound

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.theme.ThemeSettings
 *
 * @author sky
 * @since 2021/4/15 3:36 下午
 */
@Suppress("CanBeParameter")
abstract class ThemeSettings(val root: ConfigurationSection) {

    val sound: XSound? = XSound.matchXSound(root.getString("sound.name").toString()).orElse(null)
    val soundPitch = root.getDouble("sound.p").toFloat()
    val soundVolume = root.getDouble("sound.v").toFloat()

    fun playSound(session: Session) {
        if (!session.conversation.hasFlag("NO_EFFECT:SOUND")) {
            sound?.play(session.player, soundPitch, soundVolume)
        }
    }
}