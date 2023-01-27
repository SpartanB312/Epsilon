package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.TickEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import net.minecraft.init.MobEffects

internal object AntiLevitation : Module(
    name = "AntiLevitation",
    description = "Removes levitation potion effect",
    category = Category.Movement
) {
    init {
        safeListener<TickEvent.Pre> {
            player.removeActivePotionEffect(MobEffects.LEVITATION)
        }
    }
}