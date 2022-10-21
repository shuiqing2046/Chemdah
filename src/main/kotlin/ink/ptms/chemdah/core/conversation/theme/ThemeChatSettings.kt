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

    val format = root.getStringList("format")

    /** 可以选择的回复 **/
    val select = root.getString("select.reply.1", "")!!.colored()
    val selectOther = root.getString("select.reply.0", "")!!.colored()

    /** 曾被选过的回复 **/
    val selected = root.getString("select.reply-selected.1", select)!!.colored()
    val selectedOther = root.getString("select.reply-selected.0", selectOther)!!.colored()

    val talking = root.getString("talking", "")!!.colored()
    val animation = root.getBoolean("animation", true)
    val speed = root.getLong("speed", 1)
    val hoverText = root.getBoolean("hover-text", true)
    val spaceLine = root.getInt("space-line", 30)
    val useScroll = root.getBoolean("use-scroll")

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