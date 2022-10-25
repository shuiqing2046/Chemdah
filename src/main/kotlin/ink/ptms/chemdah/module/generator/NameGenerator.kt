package ink.ptms.chemdah.module.generator

import ink.ptms.chemdah.module.Module
import ink.ptms.chemdah.module.Module.Companion.register
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.releaseResourceFile
import taboolib.module.kether.*
import java.io.File

/**
 * Chemdah
 * ink.ptms.chemdah.module.generator.NameGenerator
 *
 * @author sky
 * @since 2021/3/11 9:03 上午
 */
@Awake
object NameGenerator : Module {

    /** 命名空间 **/
    val namespace = listOf("adyeshach", "chemdah", "chemdah_name_generator")

    /** 默认文件 **/
    val def = listOf("city", "dragon_1", "dragon_2", "dragon_3", "dwarf_1", "dwarf_2", "elf_1", "elf_2", "elf_3", "human", "item", "kingdom", "town")

    init {
        register()
    }

    override fun reload() {
        val folder = File(getDataFolder(), "module/generator")
        if (!folder.exists()) {
            def.forEach { releaseResourceFile("module/generator/$it.ks", false) }
        }
    }

    fun generate(name: String, amount: Int = 1): List<String> {
        val file = File(getDataFolder(), "module/generator/$name.ks")
        return if (file.exists()) {
            val readText = file.readLines().filter { !it.trimStart().startsWith('#') }
            runKether { (1..amount).map { KetherShell.eval(readText, namespace = namespace).getNow("null").toString() } }!!
        } else emptyList()
    }

    fun generatorNames(): List<String> {
        return File(getDataFolder(), "module/generator").listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }

    @KetherParser(["name"], namespace = "chemdah_name_generator")
    private fun nameParser() = scriptParser {
        val str = it.nextParsedAction()
        actionFuture { f ->
            run(str).str { n1 ->
                // uppercase first letter
                f.complete(n1.replaceFirstChar { el -> if (el.isLowerCase()) el.titlecase() else el.toString() })
            }
        }
    }
}