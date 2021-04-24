package ink.ptms.chemdah.core.quest

/**
 * Chemdah
 * ink.ptms.chemdah.core.conversation.Agent
 *
 * @author sky
 * @since 2021/2/9 6:29 下午
 */
data class Agent(
    val type: AgentType,
    val action: List<String>,
    val restrict: String? = "self"
)