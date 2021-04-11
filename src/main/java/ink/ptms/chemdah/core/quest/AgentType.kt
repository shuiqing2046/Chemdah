package ink.ptms.chemdah.core.quest

enum class AgentType(val namespace: String) {

    /**
     * 任务开始之后
     */
    QUEST_START("quest"),

    /**
     * 任务接受之前（包含任何可能的检测）
     * 不代表任务已经接受，请勿实现任何不可逆的行为
     *
     * 返回的内容决定是否继续逻辑
     */
    QUEST_ACCEPT("quest"),

    /**
     * 任务接受被取消之后（任何可能的方式）
     */
    QUEST_ACCEPT_CANCELLED("quest"),

    /**
     * 任务失败之前
     * 返回的内容决定是否继续逻辑
     */
    QUEST_FAILURE("quest"),

    /**
     * 任务完成之前
     * 返回的内容决定是否继续逻辑    QAWESRDTFGHJKM,.
     */
    QUEST_COMPLETE("quest"),

    /**
     * 任务重置之前
     * 返回的内容决定是否继续逻辑
     */
    QUEST_RESET("quest"),

    /**
     * 条目继续之前
     * 返回的内容决定是否继续逻辑
     */
    TASK_CONTINUE("task"),

    /**
     * 条目完成之前
     * 返回的内容决定是否继续逻辑
     */
    TASK_COMPLETE("task"),

    /**
     * 条目重置之前
     * 返回的内容决定是否继续逻辑
     */
    TASK_RESET("task"),

    /**
     * 无
     */
    NONE("");

    /**
     * 获取所有 Kether 命名空间
     */
    fun namespaceAll() = listOf(
        "chemdah",
        "chemdah-quest",
        "chemdah-quest-${namespace}"
    )

    companion object {

        fun String.toAgentType(): AgentType {
            return try {
                valueOf(toUpperCase().replace(" ", "_"))
            } catch (ignored: Exception) {
                NONE
            }
        }
    }
}