package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable
import net.minecraft.entity.Entity

class EntityCollisionEvent(val entity: Entity, var x: Double, var y: Double, var z: Double) : Cancellable() {}
