package ink.ptms.chemdah.util

import com.google.common.collect.Maps
import io.izzel.taboolib.util.Strings
import org.bukkit.command.CommandSender
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @Author 坏黑
 * @Since 2018-12-24 16:32
 */
class Mirror {

    val dataMap = Maps.newConcurrentMap<String, MirrorData>()!!

    fun mirrorFuture(id: String, func: MirrorFuture.() -> Unit) {
        func(MirrorFuture().also { mf ->
            mf.future.thenApply {
                finish(id, mf.startTime)
            }
        })
    }

    fun finish(id: String, startTime: Long) {
        dataMap[id]?.finish(startTime)
    }

    fun collect(opt: Options.() -> Unit = {}): MirrorCollect {
        val options = Options().also(opt)
        val collect = MirrorCollect(this, options, "/", "/")
        dataMap.entries.forEach { mirror ->
            var point = collect
            mirror.key.split(":").forEach {
                point = point.sub.computeIfAbsent(it) { _ -> MirrorCollect(this, options, mirror.key, it) }
            }
        }
        return collect
    }

    class Options {

        var childFormat = "§c[TabooLib] §8{0}§f{1} §8[{2} ms] §c[{3} ms] §7{4}%"
        var parentFormat = "§c[TabooLib] §8{0}§7{1} §8[{2} ms] §c[{3} ms] §7{4}%"
    }

    class MirrorFuture {

        val startTime = System.nanoTime()
        val future = CompletableFuture<Void>()

        fun finish() {
            future.complete(null)
        }
    }

    class MirrorCollect(
        val mirror: Mirror,
        val opt: Options,
        val key: String,
        val path: String,
        val sub: MutableMap<String, MirrorCollect> = TreeMap()
    ) {

        fun getTotal(): BigDecimal {
            var total = mirror.dataMap[key]?.timeTotal ?: BigDecimal.ZERO
            sub.values.forEach {
                total = total.add(it.getTotal())
            }
            return total
        }

        fun print(sender: CommandSender, all: BigDecimal, space: Int) {
            val spaceStr = (1..space).joinToString("") { "···" }
            val total = getTotal()
            val avg = mirror.dataMap[key]?.getAverage() ?: 0.0
            val message = Strings.replaceWithOrder(if (sub.isEmpty()) opt.childFormat else opt.parentFormat, spaceStr, path, total, avg, percent(all, total))
            sender.sendMessage(message)
            sub.values.map {
                it to percent(all, it.getTotal())
            }.sortedByDescending {
                it.second
            }.forEach {
                it.first.print(sender, all, space + 1)
            }
        }

        fun percent(all: BigDecimal, total: BigDecimal): Double {
            return if (all.toDouble() == 0.0) 0.0 else total.divide(all, 2, RoundingMode.HALF_UP).multiply(BigDecimal("100")).toDouble()
        }
    }
}