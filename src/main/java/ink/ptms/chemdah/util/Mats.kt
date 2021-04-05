package ink.ptms.chemdah.util

import com.google.common.collect.ImmutableMap
import ink.ptms.chemdah.util.Mats.InferMat.Companion.toMat
import io.izzel.taboolib.Version
import io.izzel.taboolib.internal.xseries.XBlock
import io.izzel.taboolib.internal.xseries.XMaterial
import io.izzel.taboolib.kotlin.Reflex.Companion.reflex
import io.izzel.taboolib.kotlin.Reflex.Companion.reflexInvoke
import org.bukkit.block.Block

/**
 * Chemdah
 * ink.ptms.chemdah.util.Mats
 *
 * stone[power=5] —— Matches all blocks of type STONE and which have a power of 5
 * ^stone —— Matches all blocks of type name starts with "stone"
 * stone$ —— Matches all blocks of type name ends with "stone"
 * (stone) —— Matches all blocks of type name contains "stone"
 * %slabs% —— Matches all slabs
 * *[power=5] —— Matches everything and which have a power of 5
 *
 *
 * @author sky
 * @since 2021/3/2 5:41 下午
 */
class Mats(val mats: List<InferMat>) {

    fun isBlock(block: Block): Boolean {
        val type = block.type.name.toLowerCase()
        val data = if (Version.isAfter(Version.v1_13)) {
            block.blockData.reflex<Any>("state")!!.reflexInvoke<ImmutableMap<Any, Any>>("getStateMap")!!.mapKeys { it.key.reflexInvoke<String>("getName")!! }
        } else {
            emptyMap()
        }
        return mats.any { mat -> mat.matchFlags(type) && mat.matchBlockData(data) }
    }

    data class InferMat(val material: String, val flags: List<TypeFlags>, val data: Map<String, String>) {

        fun matchFlags(type: String) = flags.any { it.match(type, material) }

        fun matchBlockData(map: Map<String, Any>) = data.all { map[it.key]?.toString() == it.value }

        companion object {

            fun String.toMat(): InferMat {
                var type: String
                val data = HashMap<String, String>()
                val flag = ArrayList<TypeFlags>()
                if (indexOf('[') > -1 && endsWith(']')) {
                    type = substring(0, indexOf('['))
                    // 只有 1.13+ 才允许加载 BlockData 选择器
                    if (Version.isAfter(Version.v1_13)) {
                        data.putAll(substring(indexOf('[') + 1, length - 1).split("[,;]".toRegex()).map {
                            it.trim().split("=").run { get(0) to (getOrNull(1) ?: get(0)) }
                        })
                    }
                } else {
                    type = this
                }
                if (type == "*") {
                    flag.add(TypeFlags.ALL)
                } else if (type.startsWith('%') && type.startsWith('%')) {
                    type = type.substring(1, type.length - 1)
                    flag.add(TypeFlags.TAG)
                } else {
                    if (type.startsWith('(') && type.startsWith(')')) {
                        type = type.substring(1, type.length - 1)
                        flag.add(TypeFlags.CONTAINS)
                    }
                    if (type.startsWith('^')) {
                        type = type.substring(1)
                        flag.add(TypeFlags.STARTS_WITH)
                    }
                    if (type.endsWith('$')) {
                        type = type.substring(0, type.length - 1)
                        flag.add(TypeFlags.ENDS_WITH)
                    }
                }
                flag.add(TypeFlags.DEFAULT)
                return InferMat(type, flag, data)
            }
        }
    }

    companion object {

        fun List<String>.toMats() = Mats(map { it.toMat() })
    }
}