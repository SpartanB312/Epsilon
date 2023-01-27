package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable

class PlayerPushEvent(var type: Type) : Cancellable() {

    enum class Type {
        BLOCK, LIQUID
    }
}
