package ink.ptms.chemdah.core.quest

enum class AgentType(val namespace: String) {

    /**
     * 任务接受
     */
    QUEST_ACCEPT("quest"),

    /**
     * 任务接受被取消
     */
    QUEST_ACCEPT_CANCELLED("quest"),

    /**
     * 任务失败
     */
    QUEST_FAILURE("quest"),

    /**
     * 任务完成
     */
    QUEST_COMPLETE("quest"),

    /**
     * 任务重置
     */
    QUEST_RESET("quest"),

    /**
     * 条目继续
     */
    TASK_CONTINUE("task"),

    /**
     * 条目完成
     */
    TASK_COMPLETE("task"),

    /**
     * 条目重置
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