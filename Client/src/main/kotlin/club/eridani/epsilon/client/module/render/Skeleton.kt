package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.RenderEntityModelEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.math.MathUtils.getInterpolatedRenderPos
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.model.ModelBiped
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11


object Skeleton :
    Module(
        name = "Skeleton",
        category = Category.Render,
        description = "Draw player moduel with line"
    ) {

    private val entities = HashMap<EntityPlayer, Array<FloatArray?>>()


    init {
        onRender3D {
            runSafe {
                club.eridani.epsilon.client.util.graphics.RenderUtils3D.glPre(1.0f)

                for (player in mc.world.playerEntities) {
                    if (player == null || player === mc.renderViewEntity || !player.isEntityAlive
                        || player.isPlayerSleeping
                        || entities[player] == null || mc.player.getDistanceSq(player) >= 2500.0
                    ) {
                        continue
                    }
                    renderSkeleton(player, entities[player])
                }

                club.eridani.epsilon.client.util.graphics.RenderUtils3D.glPost()
            }
        }

        listener<RenderEntityModelEvent> { event ->
            if (event.entity is EntityPlayer && event.modelBase is ModelBiped) {
                val rotations: Array<FloatArray?> = getBipedRotations(event.modelBase as ModelBiped)
                entities[event.entity as EntityPlayer] = rotations
            }
        }
    }

    private fun getBipedRotations(biped: ModelBiped): Array<FloatArray?> {
        val rotations = arrayOfNulls<FloatArray>(5)
        val headRotation =
            floatArrayOf(biped.bipedHead.rotateAngleX, biped.bipedHead.rotateAngleY, biped.bipedHead.rotateAngleZ)
        rotations[0] = headRotation
        val rightArmRotation = floatArrayOf(
            biped.bipedRightArm.rotateAngleX,
            biped.bipedRightArm.rotateAngleY,
            biped.bipedRightArm.rotateAngleZ
        )
        rotations[1] = rightArmRotation
        val leftArmRotation = floatArrayOf(
            biped.bipedLeftArm.rotateAngleX,
            biped.bipedLeftArm.rotateAngleY,
            biped.bipedLeftArm.rotateAngleZ
        )
        rotations[2] = leftArmRotation
        val rightLegRotation = floatArrayOf(
            biped.bipedRightLeg.rotateAngleX,
            biped.bipedRightLeg.rotateAngleY,
            biped.bipedRightLeg.rotateAngleZ
        )
        rotations[3] = rightLegRotation
        val leftLegRotation = floatArrayOf(
            biped.bipedLeftLeg.rotateAngleX,
            biped.bipedLeftLeg.rotateAngleY,
            biped.bipedLeftLeg.rotateAngleZ
        )
        rotations[4] = leftLegRotation
        return rotations
    }

    private fun renderSkeleton(player: EntityPlayer, rotations: Array<FloatArray?>?) {
        val red = GUIManager.firstColor.r
        val green = GUIManager.firstColor.g
        val blue = GUIManager.firstColor.b
//        if (FriendManager.INSTANCE.isFriend(player.name)) {
//            red = 0f
//            green = 255f
//            blue = 255f
//        }
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.pushMatrix()
        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, 1f)
        val interp = player.getInterpolatedRenderPos(mc.renderPartialTicks)
        val pX: Double = interp.x
        val pY: Double = interp.y
        val pZ: Double = interp.z
        GlStateManager.translate(pX, pY, pZ)
        GlStateManager.rotate(-player.renderYawOffset, 0.0f, 1.0f, 0.0f)
        GlStateManager.translate(0.0, 0.0, if (player.isSneaking) -0.235 else 0.0)
        val sneak = if (player.isSneaking) 0.6f else 0.75f
        GlStateManager.pushMatrix()
        GlStateManager.translate(-0.125, sneak.toDouble(), 0.0)
        if (rotations!![3]!![0] != 0.0f) {
            GlStateManager.rotate(rotations[3]!![0] * 57.295776f, 1.0f, 0.0f, 0.0f)
        }
        if (rotations[3]!![1] != 0.0f) {
            GlStateManager.rotate(rotations[3]!![1] * 57.295776f, 0.0f, 1.0f, 0.0f)
        }
        if (rotations[3]!![2] != 0.0f) {
            GlStateManager.rotate(rotations[3]!![2] * 57.295776f, 0.0f, 0.0f, 1.0f)
        }
        GlStateManager.glBegin(3)
        GL11.glVertex3d(0.0, 0.0, 0.0)
        GL11.glVertex3d(0.0, (-sneak).toDouble(), 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.125, sneak.toDouble(), 0.0)
        if (rotations[4]!![0] != 0.0f) {
            GlStateManager.rotate(rotations[4]!![0] * 57.295776f, 1.0f, 0.0f, 0.0f)
        }
        if (rotations[4]!![1] != 0.0f) {
            GlStateManager.rotate(rotations[4]!![1] * 57.295776f, 0.0f, 1.0f, 0.0f)
        }
        if (rotations[4]!![2] != 0.0f) {
            GlStateManager.rotate(rotations[4]!![2] * 57.295776f, 0.0f, 0.0f, 1.0f)
        }
        GlStateManager.glBegin(3)
        GL11.glVertex3d(0.0, 0.0, 0.0)
        GL11.glVertex3d(0.0, (-sneak).toDouble(), 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.translate(0.0, 0.0, if (player.isSneaking) 0.25 else 0.0)
        GlStateManager.pushMatrix()
        var sneakOffset = 0.0
        if (player.isSneaking) {
            sneakOffset = -0.05
        }
        GlStateManager.translate(0.0, sneakOffset, if (player.isSneaking) -0.01725 else 0.0)
        GlStateManager.pushMatrix()
        GlStateManager.translate(-0.375, sneak.toDouble() + 0.55, 0.0)
        if (rotations[1]!![0] != 0.0f) {
            GlStateManager.rotate(rotations[1]!![0] * 57.295776f, 1.0f, 0.0f, 0.0f)
        }
        if (rotations[1]!![1] != 0.0f) {
            GlStateManager.rotate(rotations[1]!![1] * 57.295776f, 0.0f, 1.0f, 0.0f)
        }
        if (rotations[1]!![2] != 0.0f) {
            GlStateManager.rotate(-rotations[1]!![2] * 57.295776f, 0.0f, 0.0f, 1.0f)
        }
        GlStateManager.glBegin(3)
        GL11.glVertex3d(0.0, 0.0, 0.0)
        GL11.glVertex3d(0.0, -0.5, 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.375, sneak.toDouble() + 0.55, 0.0)
        if (rotations[2]!![0] != 0.0f) {
            GlStateManager.rotate(rotations[2]!![0] * 57.295776f, 1.0f, 0.0f, 0.0f)
        }
        if (rotations[2]!![1] != 0.0f) {
            GlStateManager.rotate(rotations[2]!![1] * 57.295776f, 0.0f, 1.0f, 0.0f)
        }
        if (rotations[2]!![2] != 0.0f) {
            GlStateManager.rotate(-rotations[2]!![2] * 57.295776f, 0.0f, 0.0f, 1.0f)
        }
        GlStateManager.glBegin(3)
        GL11.glVertex3d(0.0, 0.0, 0.0)
        GL11.glVertex3d(0.0, -0.5, 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, sneak.toDouble() + 0.55, 0.0)
        if (rotations[0]!![0] != 0.0f) {
            GlStateManager.rotate(rotations[0]!![0] * 57.295776f, 1.0f, 0.0f, 0.0f)
        }
        GlStateManager.glBegin(3)
        GL11.glVertex3d(0.0, 0.0, 0.0)
        GL11.glVertex3d(0.0, 0.3, 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
        GlStateManager.rotate(if (player.isSneaking) 25.0f else 0.0f, 1.0f, 0.0f, 0.0f)
        if (player.isSneaking) {
            sneakOffset = -0.16175
        }
        GlStateManager.translate(0.0, sneakOffset, if (player.isSneaking) -0.48025 else 0.0)
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, sneak.toDouble(), 0.0)
        GlStateManager.glBegin(3)
        GL11.glVertex3d(-0.125, 0.0, 0.0)
        GL11.glVertex3d(0.125, 0.0, 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, sneak.toDouble(), 0.0)
        GlStateManager.glBegin(3)
        GL11.glVertex3d(0.0, 0.0, 0.0)
        GL11.glVertex3d(0.0, 0.55, 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.0, sneak.toDouble() + 0.55, 0.0)
        GlStateManager.glBegin(3)
        GL11.glVertex3d(-0.375, 0.0, 0.0)
        GL11.glVertex3d(0.375, 0.0, 0.0)
        GlStateManager.glEnd()
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }
}