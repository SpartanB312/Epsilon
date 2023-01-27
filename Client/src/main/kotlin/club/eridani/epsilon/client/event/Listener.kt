package club.eridani.epsilon.client.event

import club.eridani.epsilon.client.common.interfaces.Nameable
import club.eridani.epsilon.client.util.threads.defaultScope
import club.eridani.epsilon.client.util.threads.runSafe
import club.eridani.epsilon.client.util.threads.runSafeSuspend
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

const val DEFAULT_LISTENER_PRIORITY = 0

inline fun <reified E : Event> Any.safeListener(
    noinline function: SafeClientEvent.(E) -> Unit
) = listener(this, E::class.java, DEFAULT_LISTENER_PRIORITY, false) { runSafe { function.invoke(this, it) } }

inline fun <reified E : Event> Any.safeListener(
    priority: Int,
    noinline function: SafeClientEvent.(E) -> Unit
) = listener(this, E::class.java, priority, false) { runSafe { function.invoke(this, it) } }

inline fun <reified E : Event> Any.safeListener(
    alwaysListening: Boolean,
    noinline function: SafeClientEvent.(E) -> Unit
) = listener(this, E::class.java, DEFAULT_LISTENER_PRIORITY, alwaysListening) { runSafe { function.invoke(this, it) } }

inline fun <reified E : Event> Any.safeListener(
    priority: Int,
    alwaysListening: Boolean,
    noinline function: SafeClientEvent.(E) -> Unit
) = listener(this, E::class.java, priority, alwaysListening) { runSafe { function.invoke(this, it) } }


inline fun <reified E : Any> Any.safeParallelListener(
    noinline function: suspend SafeClientEvent.(E) -> Unit
) = parallelListener(this, E::class.java, false) { runSafeSuspend { function.invoke(this, it) } }

inline fun <reified E : Any> Any.safeParallelListener(
    alwaysListening: Boolean,
    noinline function: suspend SafeClientEvent.(E) -> Unit
) = parallelListener(this, E::class.java, alwaysListening) { runSafeSuspend { function.invoke(this, it) } }


inline fun <reified E : Any> Any.safeConcurrentListener(
    noinline function: suspend SafeClientEvent.(E) -> Unit
) = concurrentListener(this, E::class.java, false) { runSafeSuspend { function.invoke(this, it) } }

inline fun <reified E : Any> Any.safeConcurrentListener(
    alwaysListening: Boolean,
    noinline function: suspend SafeClientEvent.(E) -> Unit
) = concurrentListener(this, E::class.java, alwaysListening) { runSafeSuspend { function.invoke(this, it) } }


inline fun <reified E : Any> Any.listener(
    function: Consumer<E>
) = listener(this, E::class.java, DEFAULT_LISTENER_PRIORITY, false, function)

inline fun <reified E : Any> Any.listener(
    priority: Int,
    function: Consumer<E>
) = listener(this, E::class.java, priority, false, function)

inline fun <reified E : Any> Any.listener(
    alwaysListening: Boolean,
    function: Consumer<E>
) = listener(this, E::class.java, DEFAULT_LISTENER_PRIORITY, alwaysListening, function)

inline fun <reified E : Any> Any.listener(
    priority: Int,
    alwaysListening: Boolean,
    function: Consumer<E>
) = listener(this, E::class.java, priority, alwaysListening, function)


inline fun <reified E : Any> Any.parallelListener(
    noinline function: suspend (E) -> Unit
) = parallelListener(this, E::class.java, false, function)

inline fun <reified E : Any> Any.parallelListener(
    alwaysListening: Boolean,
    noinline function: suspend (E) -> Unit
) = parallelListener(this, E::class.java, alwaysListening, function)


inline fun <reified E : Any> Any.concurrentListener(
    noinline function: suspend (E) -> Unit
) = concurrentListener(this, E::class.java, false, function)

inline fun <reified E : Any> Any.concurrentListener(
    alwaysListening: Boolean,
    noinline function: suspend (E) -> Unit
) = concurrentListener(this, E::class.java, alwaysListening, function)

fun <E : Any> listener(
    owner: Any,
    eventClass: Class<E>,
    priority: Int,
    alwaysListening: Boolean,
    function: Consumer<E>
) {
    with(Listener(owner, eventClass, priority, function)) {
        if (alwaysListening) EventBus.subscribe(this)
        else EventBus.register(owner, this)
    }
}

fun <E : Any> parallelListener(
    owner: Any,
    eventClass: Class<E>,
    alwaysListening: Boolean,
    function: suspend (E) -> Unit
) {
    with(ParallelListener(owner, eventClass, function)) {
        if (alwaysListening) EventBus.subscribe(this)
        else EventBus.register(owner, this)
    }
}

fun <E : Any> concurrentListener(
    owner: Any,
    eventClass: Class<E>,
    alwaysListening: Boolean,
    function: suspend (E) -> Unit
) {
    with(Listener(owner, eventClass, Int.MAX_VALUE) { defaultScope.launch { function.invoke(it) } }) {
        if (alwaysListening) EventBus.subscribe(this)
        else EventBus.register(owner, this)
    }
}


class Listener<E : Any>(
    owner: Any,
    eventClass: Class<E>,
    priority: Int,
    function: Consumer<E>
) : AbstractListener<E, Consumer<E>>(owner, eventClass, priority, function)

class ParallelListener<E : Any>(
    owner: Any,
    eventClass: Class<E>,
    function: suspend (E) -> Unit
) : AbstractListener<E, suspend (E) -> Unit>(owner, eventClass, DEFAULT_LISTENER_PRIORITY, function)

sealed class AbstractListener<E : Any, F>(
    owner: Any,
    val eventClass: Class<E>,
    val priority: Int,
    val function: F
) : Comparable<AbstractListener<*, *>> {

    val id = listenerID.getAndIncrement()
    val ownerName: String = if (owner is Nameable) owner.name else owner.javaClass.simpleName

    override fun compareTo(other: AbstractListener<*, *>): Int {
        val result = other.priority.compareTo(this.priority)
        return if (result != 0) result
        else id.compareTo(other.id)
    }

    override fun equals(other: Any?): Boolean {
        return this === other
                || (other is AbstractListener<*, *>
                && other.eventClass == this.eventClass
                && other.id == this.id)
    }

    override fun hashCode(): Int {
        return 31 * eventClass.hashCode() + id.hashCode()
    }

    companion object {
        private val listenerID = AtomicInteger(Int.MIN_VALUE)
    }

}