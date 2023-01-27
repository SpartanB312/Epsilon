package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event
import club.eridani.epsilon.client.util.CrystalDamage

class CrystalSpawnEvent(
    val entityID: Int,
    val crystalDamage: CrystalDamage
) : Event()