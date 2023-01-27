package club.eridani.epsilon.client.module.movement

import baritone.api.pathing.goals.GoalXZ
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.BaritoneCommandEvent
import club.eridani.epsilon.client.event.events.ConnectionEvent
import club.eridani.epsilon.client.event.events.PlayerInputEvent
import club.eridani.epsilon.client.event.events.TickEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.player.LagBackCheck
import club.eridani.epsilon.client.util.BaritoneUtils
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.TimeUnit
import club.eridani.epsilon.client.util.math.Direction
import club.eridani.epsilon.client.util.math.fastFloor
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.util.MovementInputFromOptions
import net.minecraftforge.client.event.InputUpdateEvent

internal object AutoWalk : Module(
    name = "AutoWalk",
    category = Category.Movement,
    description = "Automatically walks somewhere"
) {
    private val mode = setting("Direction", AutoWalkMode.Baritone)
    private val disableOnDisconnect by setting("Disable On Disconnect", true)

    private enum class AutoWalkMode {
        Forward,
        Backward,
        Baritone
    }

    val baritoneWalk get() = isEnabled && mode.value == AutoWalkMode.Baritone

    private const val border = 30000000
    private val messageTimer = TickTimer(TimeUnit.SECONDS)
    var direction = Direction.NORTH; private set

    override fun isActive(): Boolean {
        return isEnabled && (mode.value != AutoWalkMode.Baritone || BaritoneUtils.isActive || BaritoneUtils.isPathing)
    }

    override fun getHudInfo(): String {
        return if (mode.value == AutoWalkMode.Baritone && (BaritoneUtils.isActive || BaritoneUtils.isPathing)) {
            direction.displayString
        } else {
            mode.value.name
        }
    }

    override fun onDisable() {
        if (mode.value == AutoWalkMode.Baritone) BaritoneUtils.cancelEverything()
    }

    init {
        listener<BaritoneCommandEvent> {
            if (it.command.contains("cancel")) {
                disable()
            }
        }

        listener<ConnectionEvent.Disconnect> {
            if (disableOnDisconnect) disable()
        }

        listener<PlayerInputEvent> {
            if (LagBackCheck.paused && LagBackCheck.pauseAutoWalk) return@listener

            if (it.movementInput !is MovementInputFromOptions) return@listener

            when (mode.value) {
                AutoWalkMode.Forward -> {
                    it.movementInput.moveForward = 1.0f
                }
                AutoWalkMode.Backward -> {
                    it.movementInput.moveForward = -1.0f
                }
                else -> {
                    // Baritone mode
                }
            }
        }

        safeListener<TickEvent.Pre> {
            if (mode.value == AutoWalkMode.Baritone && !checkBaritoneElytra() && !isActive()) {
                startPathing()
            }
        }
    }

    private fun SafeClientEvent.startPathing() {
        if (!world.isChunkGeneratedAt(player.chunkCoordX, player.chunkCoordZ)) return

        direction = Direction.fromEntity(player)
        val x = player.posX.fastFloor() + direction.directionVec.x * border
        val z = player.posZ.fastFloor() + direction.directionVec.z * border

        BaritoneUtils.cancelEverything()
        BaritoneUtils.primary?.customGoalProcess?.setGoalAndPath(GoalXZ(x, z))
    }

    private fun checkBaritoneElytra() = mc.player?.let {
        if (it.isElytraFlying && messageTimer.tickAndReset(10L)) {
            ChatUtil.sendNoSpamErrorMessage(
                "[AutoWalk] Baritone mode isn't currently compatible with Elytra flying!" +
                        " Choose a different mode if you want to use AutoWalk while Elytra flying"
            )
        }
        it.isElytraFlying
    } ?: true

    init {
        mode.listeners.add {
            if (isDisabled || mc.player == null) return@add
            if (mode.value == AutoWalkMode.Baritone) {
                if (!checkBaritoneElytra()) {
                    runSafe { startPathing() }
                }
            } else {
                BaritoneUtils.cancelEverything()
            }
        }
    }
}