package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import net.minecraft.entity.item.EntityEnderCrystal

class CrystalSetDeadEvent(
    val x: Double,
    val y: Double,
    val z: Double,
    val crystals: List<EntityEnderCrystal>
) : Event()