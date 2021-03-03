package ink.ptms.chemdah.core

import ink.ptms.chemdah.Chemdah
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor

/**
 * @author IzzelAliz
 */
class SingleListener<T : Event>(private val clazz: Class<T>, private val predicate: (T) -> Boolean, private val consumer: (T) -> Unit) : Listener,
    EventExecutor {

    override fun execute(listener: Listener, event: Event) {
        try {
            val cast = clazz.cast(event)
            if (predicate(cast)) {
                consumer(cast)
            }
        } catch (ignore: ClassCastException) {
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    companion object {

        @JvmStatic
        fun <T : Event> listen(
            clazz: Class<T>,
            priority: EventPriority = EventPriority.HIGHEST,
            ignoreCancelled: Boolean = true,
            consumer: (T) -> Unit
        ): SingleListener<T> {
            val listener = SingleListener(clazz, { it.javaClass == clazz }, consumer)
            Bukkit.getPluginManager().registerEvent(clazz, listener, priority, listener, Chemdah.plugin, ignoreCancelled)
            return listener
        }
    }
}