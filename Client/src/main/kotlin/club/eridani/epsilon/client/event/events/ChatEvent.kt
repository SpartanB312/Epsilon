package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable

class ChatEvent(var message: String) : Cancellable()