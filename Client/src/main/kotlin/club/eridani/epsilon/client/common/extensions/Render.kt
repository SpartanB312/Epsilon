package club.eridani.epsilon.client.common.extensions

import club.eridani.epsilon.client.mixin.mixins.accessor.render.*
import net.minecraft.client.renderer.DestroyBlockProgress
import net.minecraft.client.renderer.EntityRenderer
import net.minecraft.client.renderer.ItemRenderer
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.Shader
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.item.ItemStack

val DestroyBlockProgress.entityID: Int get() = (this as AccessorDestroyBlockProgress).epsilonGetEntityID()

val RenderGlobal.entityOutlineShader: ShaderGroup get() = (this as AccessorRenderGlobal).entityOutlineShader
val RenderGlobal.damagedBlocks: MutableMap<Int, DestroyBlockProgress> get() = (this as AccessorRenderGlobal).epsilonGetDamagedBlocks()
var RenderGlobal.renderEntitiesStartupCounter: Int
    get() = (this as AccessorRenderGlobal).epsilonGetRenderEntitiesStartupCounter()
    set(value) {
        (this as AccessorRenderGlobal).epsilonSetRenderEntitiesStartupCounter(value)
    }
var RenderGlobal.countEntitiesTotal: Int
    get() = (this as AccessorRenderGlobal).epsilonGetCountEntitiesTotal()
    set(value) {
        (this as AccessorRenderGlobal).epsilonSetCountEntitiesTotal(value)
    }
var RenderGlobal.countEntitiesRendered: Int
    get() = (this as AccessorRenderGlobal).epsilonGetCountEntitiesRendered()
    set(value) {
        (this as AccessorRenderGlobal).epsilonSetCountEntitiesRendered(value)
    }
var RenderGlobal.countEntitiesHidden: Int
    get() = (this as AccessorRenderGlobal).epsilonGetCountEntitiesHidden()
    set(value) {
        (this as AccessorRenderGlobal).epsilonSetCountEntitiesHidden(value)
    }

val RenderManager.renderPosX: Double get() = (this as AccessorRenderManager).renderPosX
val RenderManager.renderPosY: Double get() = (this as AccessorRenderManager).renderPosY
val RenderManager.renderPosZ: Double get() = (this as AccessorRenderManager).renderPosZ
val RenderManager.renderOutlines: Boolean get() = (this as AccessorRenderManager).renderOutlines

val ShaderGroup.listShaders: List<Shader> get() = (this as AccessorShaderGroup).listShaders
val ShaderGroup.listFrameBuffers: List<Framebuffer> get() = (this as AccessorShaderGroup).listFramebuffers



val RenderGlobal.getDamagedBlocks: Map<Int, DestroyBlockProgress> get() = (this as AccessorRenderGlobal).damagedBlocks



val ItemRenderer.prevEquippedProgressMainHand get() = (this as AccessorItemRenderer).prevEquippedProgressMainHand
var ItemRenderer.itemStackMainHand: ItemStack
    get() = (this as AccessorItemRenderer).itemStackMainHand
    set(value) {
        (this as AccessorItemRenderer).itemStackMainHand = value
    }

var ItemRenderer.equippedProgressMainHand
    get() = (this as AccessorItemRenderer).equippedProgressMainHand
    set(value) {
        (this as AccessorItemRenderer).equippedProgressMainHand = value
    }

var ItemRenderer.equippedProgressOffHand
    get() = (this as AccessorItemRenderer).equippedProgressOffHand
    set(value) {
        (this as AccessorItemRenderer).equippedProgressOffHand = value
    }

fun EntityRenderer.setupCameraTransform(partialTicks: Float, pass: Int) {
    (this as AccessorEntityRenderer).invokeSetupCameraTransform(partialTicks, pass)
}
