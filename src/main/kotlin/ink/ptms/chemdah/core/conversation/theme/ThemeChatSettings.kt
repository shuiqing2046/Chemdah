package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.core.conversation.Session
import taboolib.library.configuration.ConfigurationSection
import taboolib.library.xseries.XSound
import taboolib.module.chat.colored

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.ThemeChatSettings
 *
 * @author sky
 * @since 2021/2/12 2:08 上午
 */
class ThemeChatSettings(root: ConfigurationSection) : ThemeSettings(root) {

    val format: List<String> = root.getStringList("format").map { it.colored() }
    val select: String = root.getString("select.reply.1", "")!!.colored()
    val selectOther: String = root.getString("select.reply.0", "")!!.colored()
    val talking: String = root.getString("talking", "")!!.colored()
    val animation: Boolean = root.getBoolean("animation", true)
    val speed: Long = root.getLong("speed", 1)
    val spaceLine: Int = root.getInt("space-line", 30)
    val useScroll: Boolean = root.getBoolean("use-scroll")

    val selectSound: XSound? = XSound.matchXSound(root.getString("select.sound.name").toString()).orElse(null)
    val selectSoundPitch = root.getDouble("select.sound.p").toFloat()
    val selectSoundVolume = root.getDouble("select.sound.v").toFloat()

    val singleLineEnable = root.getBoolean("single-line.enable")
    val singleLineAutoSwap = root.getInt("single-line.auto-swap", 12)
    val singleLineReplySeparator = root.getString("single-line.reply-separator", " ")!!

    fun playSelectSound(session: Session) {
        selectSound?.play(session.player, selectSoundPitch, selectSoundVolume)
    }

    override fun toString(): String {
        return "ThemeChatSettings(format=$format, select='$select', selectOther='$selectOther', talking='$talking', animation=$animation, spaceLine=$spaceLine, useScroll=$useScroll, selectSound=$selectSound, selectSoundPitch=$selectSoundPitch, selectSoundVolume=$selectSoundVolume)"
    }
}