package ink.ptms.chemdah.core.conversation

enum class AgentType(val namespace: String) {

    BEGIN("npc"), REFUSE("player"), END("player"), NONE("player");

    companion object {

        fun String.toAgentType(): AgentType {
            return try {
                valueOf(toUpperCase())
            } catch (ignored: Exception) {
                NONE
            }
        }
    }
}