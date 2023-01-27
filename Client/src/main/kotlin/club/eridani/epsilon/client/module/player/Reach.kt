package club.eridani.epsilon.client.module.player

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module

object Reach : Module(
    name = "Reach",
    category = Category.Player,
    description = "Allows you to reach farther distances"
) {
    val reachAdd = setting("ReachAdd", 0.5f, 0f..3f, 0.01f)
}