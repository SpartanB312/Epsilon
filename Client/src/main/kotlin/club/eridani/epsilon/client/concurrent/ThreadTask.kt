package club.eridani.epsilon.client.concurrent

import club.eridani.epsilon.client.event.SafeClientEvent
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object ThreadTask {
    private val workingThreads = Runtime.getRuntime().availableProcessors()
    val executor = ThreadPoolExecutor(
        workingThreads,
        Int.MAX_VALUE,
        1000,
        TimeUnit.MILLISECONDS,
        LinkedBlockingQueue()
    )
}

fun runThreadTask(task: Runnable) {
    ThreadTask.executor.execute(task)
}


fun <T> onMainThreadSafe(block: SafeClientEvent.() -> T) =
    onMainThread { SafeClientEvent.instance?.block() }

fun <T> onMainThread(block: () -> T) =
    MainThreadExecutor.add(block)

/**
 * Runs [block] on Minecraft main thread (Client thread)
 * The [block] will the called with a [SafeClientEvent] to ensure null safety.
 *
 * @return [CompletableDeferred] callback
 *
 * @see [onMainThreadSuspend]
 */
suspend fun <T> onMainThreadSafeSuspend(block: SafeClientEvent.() -> T) =
    onMainThreadSuspend { SafeClientEvent.instance?.block() }

/**
 * Runs [block] on Minecraft main thread (Client thread)
 *
 * @return [CompletableDeferred] callback
 *
 * @see [onMainThreadSafeSuspend]
 */
suspend fun <T> onMainThreadSuspend(block: () -> T) =
    MainThreadExecutor.addSuspend(block)
