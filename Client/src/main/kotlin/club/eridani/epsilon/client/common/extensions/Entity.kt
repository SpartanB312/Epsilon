package club.eridani.epsilon.client.common.extensions

import club.eridani.epsilon.client.mixin.mixins.accessor.entity.AccessorEntityLivingBase
import net.minecraft.entity.EntityLivingBase

fun EntityLivingBase.onItemUseFinish() {
    (this as AccessorEntityLivingBase).epsilonInvokeOnItemUseFinish()
}