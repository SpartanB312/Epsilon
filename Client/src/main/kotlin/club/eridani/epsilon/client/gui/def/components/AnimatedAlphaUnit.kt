package club.eridani.epsilon.client.gui.def.components

import club.eridani.epsilon.client.gui.IFloatAnimatable
import club.eridani.epsilon.client.util.graphics.AnimationUtil

class AnimatedAlphaUnit : IFloatAnimatable {

    override var currentValue: Float = 0F

    fun update(isHoovered: Boolean, speed: Float = 0.3F) {
        currentValue = if (isHoovered) AnimationUtil.animate(100F, currentValue, speed)
        else AnimationUtil.animate(0F, currentValue, 0.15F)
    }

}