package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.*
import net.minecraft.network.play.client.CPacketEntityAction


object Sprint : Module(
    name = "Sprint",
    category = Category.Movement,
    description = "Automatically makes the player sprint"
) {
    private val mode by setting("Mode", Mode.Legit)
    private val keep = setting("Keep", true)

    var sprinting = false

    init {
        onTick {
            if (Utils.nullCheck()) return@onTick
            sprinting = (!mc.gameSettings.keyBindSneak.isKeyDown
                    && !mc.player.collidedHorizontally
                    && mc.player.foodStats.foodLevel > 6
                    && when {
                        InventoryMove.isEnabled -> mc.currentScreen != null || if (mode == Mode.Rage) mc.isPlayerMovingKeybind else mc.isPlayerMovingLegit
                        mode == Mode.Rage -> mc.isPlayerMovingKeybind
                        else -> mc.isPlayerMovingLegit
                    })
            mc.player.isSprinting = sprinting
        }

        onPacketReceive { event ->
            if (!keep.value) {
                return@onPacketReceive
            }
            if (Scaffold.isEnabled && Scaffold.mode == Scaffold.Mode.Hypixel) return@onPacketReceive

            if (event.packet is CPacketEntityAction) {
                if (event.packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                    event.cancel()
                }
            }
        }
    }

    override fun onDisable() {
        sprinting = false
    }

    override fun getHudInfo(): String {
        return mode.name
    }

    enum class Mode(val standardName: String) {
        Rage("Rage"),
        Legit("Legit")
    }
}