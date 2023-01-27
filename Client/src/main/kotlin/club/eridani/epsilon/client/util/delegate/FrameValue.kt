package club.eridani.epsilon.client.util.delegate

import club.eridani.epsilon.client.event.events.RunGameLoopEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.TimeUnit
import kotlin.reflect.KProperty

class FrameValue<T>(private val block: () -> T) {
    private var value0: T? = null
    private var lastUpdateFrame = 0

    val value: T
        get() = get()

    init {
        instances.add(this)
    }

    fun get(): T {
        return if (lastUpdateFrame == frame) {
            getLazy()
        } else {
            getForce()
        }
    }

    fun getLazy(): T {
        return value0 ?: getForce()
    }

    fun getForce(): T {
        val value = block.invoke()
        value0 = value
        lastUpdateFrame = frame

        return value
    }

    fun updateLazy() {
        value0 = null
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return get()
    }

    private companion object {
        val instances = ArrayList<FrameValue<*>>()
        val timer = TickTimer(TimeUnit.SECONDS)

        var frame = 0

        init {
            listener<RunGameLoopEvent.Render>(true) {
                frame++

                if (timer.tick(1L)) {
                    frame = 0
                    instances.forEach {
                        it.updateLazy()
                    }
                }
            }
        }
    }
}