package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module

internal object ClientSpoof : Module(
    name = "ClientSpoof",
    description = "Fakes a modless client when connecting",
    category = Category.Misc
) {

    val client by setting("Client", Client.Vanilla)

    enum class Client {
        Lunar,
        Vanilla
    }
}
