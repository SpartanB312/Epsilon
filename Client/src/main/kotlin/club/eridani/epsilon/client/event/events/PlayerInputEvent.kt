package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovementInput

class PlayerInputEvent(val player: EntityPlayer, val movementInput: MovementInput) : Event() {}
