package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable
import net.minecraft.client.entity.EntityPlayerSP

sealed class PlayerMoveEvent : Cancellable() {
    class Pre(private val player: EntityPlayerSP) : PlayerMoveEvent() {
        private val prevX = player.motionX
        private val prevY = player.motionY
        private val prevZ = player.motionZ

        val isModified: Boolean
            get() = x != prevX
                    || y != prevY
                    || z != prevZ

        var x = Double.NaN
            get() = get(field, player.motionX)

        var y = Double.NaN
            get() = get(field, player.motionY)

        var z = Double.NaN
            get() = get(field, player.motionZ)

        @Suppress("NOTHING_TO_INLINE")
        private inline fun get(x: Double, y: Double): Double {
            return when {
                cancelled -> 0.0
                !x.isNaN() -> x
                else -> y
            }
        }
    }

    object Post : PlayerMoveEvent()
}