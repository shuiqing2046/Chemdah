package ink.ptms.chemdah.module.kether

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.transform.AffineTransform
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.io.Closer
import ink.ptms.chemdah.Chemdah
import ink.ptms.chemdah.util.warning
import io.izzel.taboolib.kotlin.kether.Kether.expects
import io.izzel.taboolib.kotlin.kether.KetherParser
import io.izzel.taboolib.kotlin.kether.ScriptParser
import io.izzel.taboolib.kotlin.kether.action.supplier.ActionNull
import io.izzel.taboolib.kotlin.kether.common.api.ParsedAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestAction
import io.izzel.taboolib.kotlin.kether.common.api.QuestContext
import io.izzel.taboolib.kotlin.kether.common.loader.types.ArgTypes
import io.izzel.taboolib.util.Coerce
import org.bukkit.Location
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.concurrent.CompletableFuture


/**
 * Chemdah
 * ink.ptms.chemdah.core.quest.kether.ActionSchematic
 *
 * @author sky
 * @since 2021/2/10 6:39 下午
 */
class ActionSchematic(
    val name: ParsedAction<*>,
    val location: ParsedAction<*>,
    val rotation: ParsedAction<*>,
    val ignoreAir: Boolean
) : QuestAction<Void>() {

    override fun process(frame: QuestContext.Frame): CompletableFuture<Void> {
        return frame.newFrame(name).run<Any>().thenAccept { name ->
            frame.newFrame(location).run<Location>().thenAccept { location ->
                frame.newFrame(rotation).run<Any?>().thenAccept { rotation ->
                    var f = File(Chemdah.plugin.dataFolder, "schematics/${name}.schematic")
                    if (!f.exists()) {
                        f = File(Chemdah.plugin.dataFolder, "schematics/${name}.schem")
                    }
                    if (!f.exists()) {
                        warning("Schematic $name does not exist!")
                    } else {
                        val format = ClipboardFormats.findByAlias("schematic")
                        if (format == null) {
                            warning("Unknown schematic format: schematic")
                        } else {
                            try {
                                Closer.create().use { closer ->
                                    val editSession = WorldEdit.getInstance().editSessionFactory.getEditSession(BukkitWorld(location.world), -1)
                                    val fis = closer.register(FileInputStream(f))
                                    val bis = closer.register(BufferedInputStream(fis))
                                    val reader = format.getReader(bis)
                                    val clipboard = reader.read()
                                    val transform = AffineTransform().also {
                                        if (rotation != null) {
                                            it.rotateY(Coerce.toDouble(rotation))
                                        }
                                    }
                                    val holder = ClipboardHolder(clipboard)
                                    holder.transform = holder.transform.combine(transform)
                                    Operations.complete(
                                        holder.createPaste(editSession)
                                            .to(BlockVector3.at(location.blockX, location.blockY, location.blockZ))
                                            .ignoreAirBlocks(ignoreAir)
                                            .build()
                                    )
                                }
                            } catch (t: IOException) {
                                warning("Schematic could not read or it does not exist: ${t.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {

        /**
         * schematic *ship at location *world *0 *0 *0 ignore air
         * schematic *ship at location *world *0 *0 *0 rotation random array [ *0 *90 *180 *270 ] ignore air
         */
        @KetherParser(["schematic", "schem"], namespace = "chemdah")
        fun parser() = ScriptParser.parser {
            val name = it.next(ArgTypes.ACTION)
            it.expects("at", "on", "to")
            val location = it.next(ArgTypes.ACTION) as ParsedAction<*>
            val rotation = try {
                it.mark()
                it.expect("rotation")
                it.next(ArgTypes.ACTION)
            } catch (ex: Throwable) {
                it.reset()
                ParsedAction(ActionNull())
            }
            val ignoreAir = try {
                it.mark()
                it.expect("ignore")
                it.expect("air")
                true
            } catch (ex: Throwable) {
                it.reset()
                false
            }
            ActionSchematic(name, location, rotation, ignoreAir)
        }
    }
}