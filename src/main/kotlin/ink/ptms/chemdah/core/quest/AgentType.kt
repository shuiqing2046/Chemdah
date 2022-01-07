package ink.ptms.chemdah.core.quest

enum class AgentType(val namespace: String) {

    /**
     * 任务接受之前（包含任何可能的检测）
     * 不代表任务已经接受，请勿实现任何不可逆的行为
     *
     * 返回的内容决定是否继续逻辑
     */
    QUEST_ACCEPT("quest"),

    /**
     * 任务接受之后
     */
    QUEST_ACCEPTED("quest"),

    /**
     * 任务接受被取消之后（任何可能的方式）
     */
    QUEST_ACCEPT_CANCELLED("quest"),

    /**
     * 任务失败之前
     * 不代表任务已经失败，请勿实现任何不可逆的行为
     *
     * 返回的内容决定是否继续逻辑
     */
    QUEST_FAIL("quest"),

    /**
     * 任务失败之后
     */
    QUEST_FAILED("quest"),

    /**
     * 任务完成之前
     * 不代表任务已经完成，请勿实现任何不可逆的行为
     *
     * 返回的内容决定是否继续逻辑
     */
    QUEST_COMPLETE("quest"),

    /**
     * 任务完成之后
     */
    QUEST_COMPLETED("quest"),

    /**
     * 任务重启之前
     * 不代表任务已经重启，请勿实现任何不可逆的行为
     *
     * 返回的内容决定是否继续逻辑
     */
    QUEST_RESTART("quest"),

    /**
     * 任务重启之后
     */
    QUEST_RESTARTED("quest"),

    /**
     * 条目继续之后
     */
    TASK_CONTINUED("task"),

    /**
     * 条目重置之后
     */
    TASK_RESTARTED("task"),

    /**
     * 条目完成之后
     */
    TASK_COMPLETED("task"),

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
                valueOf(uppercase().replace("[ :]".toRegex(), "_"))
            } catch (ignored: Exception) {
                NONE
            }
        }
    }
}