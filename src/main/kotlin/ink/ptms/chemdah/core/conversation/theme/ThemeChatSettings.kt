package ink.ptms.chemdah.core.conversation.theme

import ink.ptms.chemdah.core.conversation.LineFormat
import ink.ptms.chemdah.core.conversation.Session
import ink.ptms.chemdah.util.mapSection
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

    /** 对话格式 **/
    val format = root.getStringList("format")

    /** 对话单行格式 **/
    val formatLine = root.mapSection("format-line") { LineFormat(it) }

    /** 标准回复模板 **/
    val select = ReplyFormat(root.getConfigurationSection("select.reply")!!)

    /** 曾被选过的回复模板 **/
    val selected = ReplyFormat(root.getConfigurationSection("select.reply-selected")!!)

    /** 自定义回复模板 **/
    val customSelect = root.mapSection("select.reply-custom") { ReplyFormat(it) }

    /** NPC 正在发言时显示回复内容 **/
    val talking = root.getString("talking", "")!!.colored()

    /** 是否启用动画 **/
    val animation = root.getBoolean("animation", true)

    /** 动画播放速度 **/
    val speed = root.getLong("speed", 1)

    /** 是否在回复上以悬浮字形式显示回复文本 **/
    val hoverText = root.getBoolean("hover-text", true)

    /** 空行 **/
    val spaceLine = root.getInt("space-line", 30)

    /** 是否启用滚轮切换回复 **/
    val useScroll = root.getBoolean("use-scroll")

    /** 音效 **/
    val selectSound: XSound? = XSound.matchXSound(root.getString("select.sound.name").toString()).orElse(null)
    val selectSoundPitch = root.getDouble("select.sound.p").toFloat()
    val selectSoundVolume = root.getDouble("select.sound.v").toFloat()

    /** 是否启用单行回复 **/
    val singleLineEnable = root.getBoolean("single-line.enable")

    /** 单行回复自动换行长度 **/
    val singleLineAutoSwap = root.getInt("single-line.auto-swap", 12)

    /** 单行回复每个回复之间的间隔文本 **/
    val singleLineReplySeparator = root.getString("single-line.reply-separator", " ")!!

    /** 自动空行填充 **/
    val spaceFilling = root.getInt("space-filling", 5)

    fun playSelectSound(session: Session) {
        selectSound?.play(session.player, selectSoundPitch, selectSoundVolume)
    }

    class ReplyFormat(val root: ConfigurationSection) {

        /** 位于当前选项 **/
        val select = root.getString("1", "")!!.colored()

        /** 位于其他选项 **/
        val other = root.getString("0", "")!!.colored()

        override fun toString(): String {
            return "ReplyFormat(select='$select', other='$other')"
        }
    }
}