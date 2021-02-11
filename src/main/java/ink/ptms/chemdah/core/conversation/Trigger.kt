package ink.ptms.chemdah.core.conversation

/**
 * Chemdah
 * ink.ptms.chemdah.core.NpcId
 *
 * @author sky
 * @since 2021/2/9 6:19 下午
 */
data class Trigger(
    val id: List<Id>
) {

    data class Id(
        val namespace: String,
        val value: String
    )
}