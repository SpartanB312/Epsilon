package club.eridani.epsilon.client.event.decentralized.events.render

import club.eridani.epsilon.client.event.decentralized.CancellableEventData
import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent

object RenderOverlayDecentralizedEvent : DataDecentralizedEvent<RenderOverlayDecentralizedEvent.RenderOverlayData>() {

    data class RenderOverlayData(val type: OverlayType) : CancellableEventData(this)

    enum class OverlayType {
        FIRE, BLOCK, WATER
    }

}