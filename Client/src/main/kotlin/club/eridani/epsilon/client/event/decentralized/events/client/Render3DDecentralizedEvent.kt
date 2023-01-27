package club.eridani.epsilon.client.event.decentralized.events.client

import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.EventData

object Render3DDecentralizedEvent : DataDecentralizedEvent<Render3DDecentralizedEvent.Render3DEventData>() {
    class Render3DEventData(val partialTicks: Float) : EventData(this)
}