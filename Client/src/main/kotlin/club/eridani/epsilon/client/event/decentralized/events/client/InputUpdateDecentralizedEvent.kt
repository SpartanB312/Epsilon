package club.eridani.epsilon.client.event.decentralized.events.client

import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.EventData

object InputUpdateDecentralizedEvent : DataDecentralizedEvent<InputUpdateDecentralizedEvent.InputUpdateEventData>() {
    class InputUpdateEventData(val key: Int, val character: Char) : EventData(this)
}