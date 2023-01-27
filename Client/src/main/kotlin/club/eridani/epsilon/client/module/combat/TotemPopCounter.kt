package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module

object TotemPopCounter : Module(
    name = "TotemPopCounter",
    category = Category.Combat,
    description = "Counts the times your enemy pops"
) {
    val notification by setting("Notification", false)
    val chat by setting("UesChat", true)
    val rawChat by setting("RawChat Message", false) { chat }
}