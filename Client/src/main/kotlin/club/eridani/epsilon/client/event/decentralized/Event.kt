@file:Suppress("UNCHECKED_CAST")

package club.eridani.epsilon.client.event.decentralized

import club.eridani.epsilon.client.common.extensions.addIf
import club.eridani.epsilon.client.event.Event
import club.eridani.epsilon.client.event.EventBus
import club.eridani.epsilon.client.util.Wrapper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by B_312 on 08/22/2021
 */
interface IDecentralizedEvent<T : Any> {
    val registeredTasks: ArrayList<RegisteredListener<T>>

    fun register(owner: Any, priority: Int, action: (T) -> Unit) {
        register(RegisteredListener(owner, priority, action))
    }

    fun register(listener: RegisteredListener<T>) {
        synchronized(registeredTasks) {
            registeredTasks.addIf(listener) {
                !it.contains(listener)
            }
            registeredTasks.sortedByDescending { it.priority }
        }
    }

    fun unregister(owner: Any) {
        synchronized(registeredTasks) {
            registeredTasks.removeIf {
                it.owner == owner
            }
        }
    }

    fun post(data: T) {
        synchronized(registeredTasks) {
            registeredTasks.forEach {
                runBlocking {
                    if (it.priority == Priority.PARALLEL) launch { it.action.invoke(data) }
                    else it.action.invoke(data)
                }
            }
        }
        EventBus.post(data)
    }
}

interface Listenable {

    val subscribedListener: ArrayList<Triple<IDecentralizedEvent<*>, (Any) -> Unit, Int>>

    fun subscribe() {
        synchronized(subscribedListener) {
            subscribedListener.forEach { (event, priority, action) ->
                event.unregister(this)
                event.register(this, action, priority)
            }
        }
    }

    fun unsubscribe() {
        synchronized(subscribedListener) {
            subscribedListener.forEach { (event, _) ->
                event.unregister(this)
            }
        }
    }

}

fun <T : Any> Listenable.decentralizedListener(event: IDecentralizedEvent<T>, priority: Int, action: (T) -> Unit) {
    synchronized(subscribedListener) {
        subscribedListener.addIf(Triple(event, action as (Any) -> Unit, priority)) { it ->
            it.all { it.second != action }
        }
    }
}

fun <T : Any> Listenable.decentralizedListener(
    event: IDecentralizedEvent<T>,
    action: (T) -> Unit
) = this.decentralizedListener(event, Priority.MEDIUM, action)

fun <T : Any> Listenable.parallelListener(
    event: IDecentralizedEvent<T>,
    action: (T) -> Unit
) = this.decentralizedListener(event, Priority.PARALLEL, action)

object Priority {
    const val LOWEST = Int.MIN_VALUE
    const val LOW = -1000
    const val MEDIUM = 0
    const val HIGH = 1000
    const val HIGHEST = Int.MAX_VALUE - 1
    const val PARALLEL = Int.MAX_VALUE
}

open class NonDataDecentralizedEvent : IDecentralizedEvent<EventData> {
    override val registeredTasks = ArrayList<RegisteredListener<EventData>>()

    fun post() {
        post(EventData(this))
        //EventBus.post(this)
    }
}

open class DataDecentralizedEvent<T : EventData> : IDecentralizedEvent<T> {
    override val registeredTasks = ArrayList<RegisteredListener<T>>()
}

open class EventData(open val father: IDecentralizedEvent<*>) : Event()

open class ProfiledDecentralizedEvent(private val profilerName: String) : NonDataDecentralizedEvent() {
    override fun post(data: EventData) {
        Wrapper.mc.profiler.startSection(profilerName)
        super.post(data)
        Wrapper.mc.profiler.endSection()
    }
}

open class CancellableEventData(override val father: IDecentralizedEvent<*>) : EventData(father) {
    var cancelled = false

    fun cancel() {
        cancelled = true
    }
}

class RegisteredListener<T>(val owner: Any, val priority: Int, val action: (T) -> Unit) {
    override fun equals(other: Any?): Boolean {
        return if (other != null && other is RegisteredListener<*>) {
            other.action == action && other.owner == owner
        } else false
    }

    override fun hashCode(): Int {
        return owner.hashCode() + action.hashCode()
    }
}