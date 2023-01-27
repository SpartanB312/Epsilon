package club.eridani.epsilon.client.util.extension

import club.eridani.epsilon.client.common.extensions.renderPosX
import club.eridani.epsilon.client.common.extensions.renderPosY
import club.eridani.epsilon.client.common.extensions.renderPosZ
import club.eridani.epsilon.client.common.interfaces.Helper
import net.minecraft.util.math.AxisAlignedBB

object AxisAlignedBB : Helper {

    fun AxisAlignedBB.interp(): AxisAlignedBB {
        return this.offset(
            -mc.renderManager.renderPosX,
            -mc.renderManager.renderPosY,
            -mc.renderManager.renderPosZ
        )
    }

}