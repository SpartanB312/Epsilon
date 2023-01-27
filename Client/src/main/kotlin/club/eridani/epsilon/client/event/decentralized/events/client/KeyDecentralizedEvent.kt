package club.eridani.epsilon.client.event.decentralized.events.client

import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.EventData

object KeyDecentralizedEvent : DataDecentralizedEvent<KeyDecentralizedEvent.KeyEventData>() {
    class KeyEventData(val key: Int, val character: Char) : EventData(this)
}