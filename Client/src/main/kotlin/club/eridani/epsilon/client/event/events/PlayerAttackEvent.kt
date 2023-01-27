package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable
import net.minecraft.entity.Entity

class PlayerAttackEvent(val entity: Entity) : Cancellable()