package club.eridani.epsilon.client.module.player

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module

object Hitbox : Module(
    name = "Hitbox",
    description = "Change entities bounding box",
    category = Category.Player
) {
    private val minSize by setting("Min-Size", 0.1, 0.1..0.8, 0.01)
    private val maxSize by setting("Max-Size", 0.25, 0.1..1.0, 0.01)

    @JvmStatic
    fun getSize(): Float {
        val min = minSize.coerceAtMost(maxSize)
        val max = minSize.coerceAtLeast(maxSize)
        return (if (isEnabled) Math.random() * (max - min) + min else 0.1f).toFloat()
    }

}