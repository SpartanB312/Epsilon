package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import net.minecraft.entity.EntityLivingBase

sealed class CombatEvent : Event() {
    abstract val entity: EntityLivingBase?

    class UpdateTarget(val prevEntity: EntityLivingBase?, override val entity: EntityLivingBase?) : CombatEvent()
}
