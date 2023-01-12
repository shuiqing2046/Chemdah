package ink.ptms.chemdah

import ink.ptms.adyeshach.core.Adyeshach
import taboolib.common.util.unsafeLazy

/**
 * Chemdah
 * ink.ptms.chemdah.AdyeshachChecker
 *
 * @author 坏黑
 * @since 2023/1/5 17:14
 */
object AdyeshachChecker {

    /**
     * 是否为 v2 版本
     */
    val isNewVersion by unsafeLazy { kotlin.runCatching { Adyeshach.api() }.isSuccess }

    /**
     * 是否为 v1 版本
     */
    val isLegacyVersion by unsafeLazy { !isNewVersion }
}