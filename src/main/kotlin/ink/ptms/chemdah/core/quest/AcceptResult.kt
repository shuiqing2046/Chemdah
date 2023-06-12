package ink.ptms.chemdah.core.quest

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.AcceptResult
 *
 * @author sky
 * @since 2021/3/4 11:52 下午
 */
data class AcceptResult(val type: Type, val reason: String? = null) {

    enum class Type {

        /**
         * 已存在
         */
        ALREADY_EXISTS,

        /**
         * 取消
         */
        CANCELLED,

        /**
         * 取消（通过控制组件）
         */
        CANCELLED_BY_CONTROL,

        /**
         * 取消（通过脚本代理）
         */
        CANCELLED_BY_AGENT,

        /**
         * 取消（通过事件）
         */
        CANCELLED_BY_EVENT,

        /**
         * 成功
         */
        SUCCESSFUL,

        /**
         * 失败
         */
        FAILED
    }
}