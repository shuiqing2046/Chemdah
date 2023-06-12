package ink.ptms.chemdah.util

import taboolib.common5.cbool
import java.util.concurrent.CompletableFuture

fun <T> CompletableFuture<T>.thenTrue(proc: () -> Unit) {
    thenApply { if (it.cbool) proc() }
}

fun <T> CompletableFuture<T>.thenBool(proc: ProcessBool.() -> Unit) {
    val processBool = ProcessBool().also(proc)
    thenApply { if (it.cbool) processBool.runTrue() else processBool.runElse() }
}

fun <T> CompletableFuture<T>.except(fn: (Throwable) -> T): CompletableFuture<T> {
    return exceptionally {
        it.printStackTrace()
        fn(it)
    }
}

fun <T> CompletableFuture<T>.exceptNull(fn: (Throwable) -> Unit): CompletableFuture<T> {
    return exceptionally {
        it.printStackTrace()
        fn(it)
        null
    }
}

class ProcessBool {

    private var trueFunc: (() -> Unit)? = null
    private var elseFunc: (() -> Unit)? = null

    fun ifTrue(func: () -> Unit) {
        trueFunc = func
    }

    fun orElse(func: () -> Unit) {
        elseFunc = func
    }

    fun runTrue() {
        trueFunc?.invoke()
    }

    fun runElse() {
        elseFunc?.invoke()
    }
}