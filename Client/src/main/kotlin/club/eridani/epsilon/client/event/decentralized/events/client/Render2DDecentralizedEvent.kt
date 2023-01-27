package club.eridani.epsilon.client.event.decentralized.events.client

import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.EventData
import net.minecraft.client.gui.ScaledResolution

object Render2DDecentralizedEvent : DataDecentralizedEvent<Render2DDecentralizedEvent.Render2DEventData>() {
    class Render2DEventData(val resolution: ScaledResolution) : EventData(this)
}
