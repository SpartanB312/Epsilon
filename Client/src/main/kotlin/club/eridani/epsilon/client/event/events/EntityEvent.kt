package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import net.minecraft.entity.EntityLivingBase

sealed class EntityEvent(val entity: EntityLivingBase) : Event() {
    class UpdateHealth(entity: EntityLivingBase, val prevHealth: Float, val health: Float) : EntityEvent(entity)

    class Death(entity: EntityLivingBase) : EntityEvent(entity)
}