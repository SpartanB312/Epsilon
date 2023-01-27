package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Event

sealed class RunGameLoopEvent : Event() {
    object Start : RunGameLoopEvent()
    object Tick : RunGameLoopEvent()
    object Render : RunGameLoopEvent()
    object End : RunGameLoopEvent()
}