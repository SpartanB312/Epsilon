package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event

sealed class ConnectionEvent : Event() {
    object Connect : ConnectionEvent()
    object Disconnect : ConnectionEvent()
}