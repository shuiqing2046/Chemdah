package ink.ptms.chemdah.module.ui

enum class ItemType(val priority: Int) {

    /**
     * 任务组信息
     */
    INFO(0),

    /**
     * 任务组过滤
     */
    FILTER(0),

    /**
     * 任务正在进行中
     */
    QUEST_STARTED(11),

    /**
     * 任务正在进行中
     */
    QUEST_STARTED_SHARED(10),

    /**
     * 任务可以开始
     */
    QUEST_CAN_START(9),

    /**
     * 任务无法开始
     */
    QUEST_CANNOT_START(8),

    /**
     * 任务已完成
     */
    QUEST_COMPLETE(7),

    /**
     * 占位符
     */
    QUEST_UNAVAILABLE(0)

}