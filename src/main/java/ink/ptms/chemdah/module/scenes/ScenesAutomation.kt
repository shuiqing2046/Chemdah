package ink.ptms.chemdah.module.scenes

import ink.ptms.chemdah.util.asInt
import ink.ptms.chemdah.util.asList
import io.izzel.taboolib.util.Coerce

/**
 * Chemdah
 * ink.ptms.chemdah.module.scenes.Automation
 *
 * @author sky
 * @since 2021/5/14 9:58 上午
 */
class ScenesAutomation(val root: Map<String, Any>) {

    val condition = root["if"]?.asList()
    val state = root["state"].asInt()
    val marge = Coerce.toBoolean(root["merge"])
}