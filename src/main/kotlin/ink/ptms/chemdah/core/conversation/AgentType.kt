package ink.ptms.chemdah.core.conversation

enum class AgentType(val namespace: String) {

    /**
     * 对话即将开始
     */
    BEGIN("npc"),

    /**
     * 对话已经开始
     */
    START("npc"),

    /**
     * 对话放弃
     */
    REFUSE("player"),

    /**
     * 对话结束
     */
    END("player"),

    /**
     * 无
     */
    NONE("");

    /**
     * 获取所有 Kether 命名空间
     */
    fun namespaceAll() = listOf(
        "chemdah",
        "chemdah-conversation",
        "chemdah-conversation-${namespace}",
        "adyeshach"
    )

    companion object {

        fun String.toAgentType(): AgentType {
            return try {
                valueOf(uppercase())
            } catch (ignored: Exception) {
                NONE
            }
        }
    }
}