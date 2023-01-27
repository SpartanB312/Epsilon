package club.eridani.epsilon.client.concurrent

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.LongSupplier
import kotlin.system.measureTimeMillis

class RepeatUnit(
    val delayMs: Long = 1000L,
    val delaySupplier: (LongSupplier)? = null,
    val task: () -> Unit,
    private var suspended: AtomicBoolean = AtomicBoolean(false),
    scope: CoroutineScope
) {

    private val job = scope.launch {
        while (true) {
            val passedTime = measureTimeMillis {
                if (!suspended.get()) task.invoke()
            }
            delay(
                if (passedTime >= (delaySupplier?.asLong ?: delayMs)) 0
                else (delaySupplier?.asLong ?: delayMs) - passedTime
            )
        }
    }

    fun suspend() {
        suspended.set(true)
    }

    fun resume() {
        suspended.set(false)
    }

    fun cancel() {
        try {
            job.cancel()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

}