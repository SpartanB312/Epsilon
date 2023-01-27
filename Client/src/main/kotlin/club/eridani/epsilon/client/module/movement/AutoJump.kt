package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.TimeUnit

internal object AutoJump : Module(
    name = "AutoJump",
    category = Category.Movement,
    description = "Automatically jumps if possible"
) {
    private val delay = setting("Tick Delay", 10, 0..40, 1)

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<PlayerMoveEvent.Pre> {
            if (player.isInWater || player.isInLava) player.motionY = 0.1
            else if (player.onGround && timer.tickAndReset(delay.value)) player.jump()
        }
    }
}