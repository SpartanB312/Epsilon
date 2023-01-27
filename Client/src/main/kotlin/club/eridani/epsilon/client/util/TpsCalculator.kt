package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.Event
import net.minecraft.launchwrapper.LogWrapper
import net.minecraft.network.play.server.SPacketTimeUpdate
import net.minecraft.util.math.MathHelper
import java.util.*

object TpsCalculator : Helper, Event() {
    private val tickRates = FloatArray(20)
    private var nextIndex = 0
    private var timeLastTimeUpdate: Long = 0

    val tickRate: Float
        get() {
            var numTicks = 0.0f
            var sumTickRates = 0.0f
            for (tickRate in tickRates) {
                if (tickRate > 0.0f) {
                    sumTickRates += tickRate
                    numTicks += 1.0f
                }
            }
            return MathHelper.clamp(sumTickRates / numTicks, 0.0f, 20.0f)
        }

    init {
        reset()
        onPacketReceive {
            if (it.packet is SPacketTimeUpdate) {
                onTimeUpdate()
            }
        }
    }


    fun getMultiplier(): Float {
        return 20.0f / tickRate
    }

    fun reset() {
        nextIndex = 0
        timeLastTimeUpdate = -1L
        Arrays.fill(tickRates, 0.0f)
    }


    private fun onTimeUpdate() {
        if (timeLastTimeUpdate != -1L) {
            val timeElapsed = (System.currentTimeMillis() - timeLastTimeUpdate).toFloat() / 1000.0f
            tickRates[nextIndex % tickRates.size] = MathHelper.clamp(20.0f / timeElapsed, 0.0f, 20.0f)
            nextIndex += 1
        }
        timeLastTimeUpdate = System.currentTimeMillis()
    }

    fun globalInfoPingValue(): Int {
        return when {
            mc.connection == null -> { // tested, this is not null in mp
                1
            }
            mc.player == null -> { // this actually takes about 30 seconds to load in Minecraft
                -1
            }
            else -> {
                try {
                    return mc.connection!!.getPlayerInfo(mc.player.uniqueID).responseTime
                } catch (npe: NullPointerException) {
                    LogWrapper.info("Caught NPE l25 CalcPing.java")
                }
                -1
            }
        }
    }
}