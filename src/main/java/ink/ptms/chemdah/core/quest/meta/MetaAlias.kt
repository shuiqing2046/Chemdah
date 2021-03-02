package ink.ptms.chemdah.core.quest.meta

import ink.ptms.chemdah.core.quest.Id

/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.meta.MetaAliases
 *
 * @author sky
 * @since 2021/3/1 11:47 下午
 */
@Id("alias")
class MetaAlias(source: Any) : Meta(source) {

    val alias = source.toString()
}