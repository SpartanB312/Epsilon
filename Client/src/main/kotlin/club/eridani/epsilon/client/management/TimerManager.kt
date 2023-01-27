package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.common.collections.lastValueOrNull
import club.eridani.epsilon.client.common.collections.synchronized
import club.eridani.epsilon.client.common.extensions.tickLength
import club.eridani.epsilon.client.common.extensions.timer
import club.eridani.epsilon.client.event.events.RunGameLoopEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.util.Wrapper.mc
import club.eridani.epsilon.client.util.threads.runSafe
import java.util.*
import kotlin.math.roundToInt

object TimerManager {
    private val modifiers = TreeMap<AbstractModule, Modifier>().synchronized()
    private var modified = false

    var totalTicks = Int.MIN_VALUE
    var tickLength = 50.0f; private set

    init {
        listener<RunGameLoopEvent.Start>(Int.MAX_VALUE, true) {
            runSafe {
                synchronized(modifiers) {
                    modifiers.values.removeIf { it.endTick < totalTicks }
                    modifiers.lastValueOrNull()?.let {
                        mc.timer.tickLength = it.tickLength
                    } ?: return@runSafe null
                }

                modified = true
            } ?: run {
                modifiers.clear()
                if (modified) {
                    mc.timer.tickLength = 50.0f
                    modified = false
                }
            }

            tickLength = mc.timer.tickLength
        }

        listener<RunGameLoopEvent.Tick>(Int.MAX_VALUE, true) {
            totalTicks++
        }
    }

    fun AbstractModule.resetTimer() {
        modifiers.remove(this)
    }

    fun AbstractModule.modifyTimer(tickLength: Float, timeoutTicks: Int = 1) {
        runSafe {
            modifiers[this@modifyTimer] = Modifier(tickLength, club.eridani.epsilon.client.management.TimerManager.totalTicks + club.eridani.epsilon.client.util.graphics.RenderUtils3D.partialTicks.roundToInt() + timeoutTicks)
        }
    }

    private class Modifier(
        val tickLength: Float,
        val endTick: Int
    )
}