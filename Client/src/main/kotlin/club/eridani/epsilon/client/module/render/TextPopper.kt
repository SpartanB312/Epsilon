package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.management.Fonts
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.math.MathUtils
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketDestroyEntities
import net.minecraft.network.play.server.SPacketEntityStatus
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

object TextPopper :
    Module(name = "TextPopper", category = Category.Render, description = "Skid ZeroDay's SuperheroFX") {

    private val explosionDelay by setting("Explosion Delay", 1f, 0f..5f, 0.1f)
    private val hitDelay by setting("Hit Delay", 1f, 0.1f..5f, 0.1f)
    private val scaling by setting("Scale", 3f, 1f..20f, 0.1f)
    private val duration by setting("Duration", 1.0, 0.1..10.0, .01)
    private val xRotate by setting("RotateX", true)
    private val zRotate by setting("RotateZ", true)

    val font by setting("Font", Font.Osaka)

    private val superHeroTextsBlowup = arrayOf("KABOOM", "BOOM", "POW", "KAPOW")
    private val superHeroTextsDamageTaken = arrayOf("OUCH", "ZAP", "BAM", "WOW", "POW", "SLAP")
    private val popTexts = CopyOnWriteArrayList<PopupText>()
    private val hitTimer = Timer()
    private val explosionTimer = Timer()

    init {
        onTick {
            popTexts.removeIf { it.isMarked }
            popTexts.forEach { it.update() }
        }

        onRender3D { event ->
            val fontRenderer = when (font) {
                Font.Osaka -> Fonts.osaka
                Font.Knight -> Fonts.knight
                Font.Badaboom -> Fonts.badaboom
            }
            popTexts.forEach { pop ->
                val entity2 = mc.renderViewEntity
                if (entity2 != null) {
                    var pos = MathUtils.getInterpolateVec3dPos(pop.pos, event.partialTicks)
                    val n = pos.x
                    var distance = pos.y + 0.65
                    val n2 = pos.z
                    val n3 = distance
                    pos = MathUtils.getInterpolateEntityClose(entity2, event.partialTicks)
                    val posX = entity2.posX
                    val posY = entity2.posY
                    val posZ = entity2.posZ
                    entity2.posX = pos.x
                    entity2.posY = pos.y
                    entity2.posZ = pos.z
                    distance = entity2.getDistance(n, distance, n2)
                    var scale = 0.04
                    if (distance > 0.0) {
                        scale = 0.02 + (scaling / 1000.0f).toDouble() * distance
                    }
                    GlStateManager.pushMatrix()
                    GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
                    GlStateManager.translate(n.toFloat(), n3.toFloat() + 1.4f, n2.toFloat())
                    val n7 = -mc.renderManager.playerViewY

                    GlStateManager.rotate(n7, pop.xIncrease, 1.0f, pop.zIncrease)
                    GlStateManager.rotate(mc.renderManager.playerViewX, if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f, 0.0f, 0.0f)
                    GlStateManager.scale(-scale, -scale, scale)
                    val nameTag = pop.displayName
                    val width = fontRenderer.getWidth(nameTag, 0.2f) / 2f
                    val height = fontRenderer.getHeight(0.2f)
                    fontRenderer.drawStringWithShadow(nameTag, -width + 1.0f, -height + 3.0f, pop.color, 0.2f)
                    GlStateManager.disablePolygonOffset()
                    GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
                    GlStateManager.popMatrix()
                    entity2.posX = posX
                    entity2.posY = posY
                    entity2.posZ = posZ
                }
            }
        }

        onPacketReceive { event ->
            runSafe {
                if (event.packet !is SPacketExplosion) {
                    if (event.packet !is SPacketEntityStatus) {
                        if (event.packet is SPacketDestroyEntities) {
                            val packet = event.packet
                            for (id in packet.entityIDs) {
                                val e = mc.world.getEntityByID(id) ?: return@runSafe
                                if (e.isDead && mc.player.getDistance(e) < 20.0f && e != mc.player) {
                                    if (e is EntityPlayer) {
                                        val pos = Vec3d(e.posX + Random.nextDouble(), e.posY + Random.nextDouble() - 2.0, e.posZ + Random.nextDouble())
                                        popTexts.add(PopupText("EZ", pos))
                                    }
                                }
                            }
                        }
                    } else {
                        val packet2 = event.packet
                        if (mc.world == null) {
                            return@runSafe
                        }
                        val e2 = packet2.getEntity(mc.world) ?: return@runSafe
                        if (packet2.opCode.toInt() != 35) {
                            if (mc.player.getDistance(e2) < 20.0f && e2 != mc.player) {
                                val pos2 = Vec3d(e2.posX + Random.nextDouble(), e2.posY + Random.nextDouble() - 2.0, e2.posZ + Random.nextDouble())
                                if (hitTimer.passed((hitDelay * 1000.0f).toInt())) {
                                    hitTimer.reset()
                                    popTexts.add(PopupText(superHeroTextsDamageTaken[Random.nextInt(superHeroTextsBlowup.size)], pos2))
                                }
                            }
                        } else if (mc.player.getDistance(e2) < 20.0f) {
                            popTexts.add(PopupText("POP", e2.positionVector.add((Random.nextInt(2) / 2).toDouble(), 1.0, (Random.nextInt(2) / 2).toDouble())))
                        }
                    }
                } else {
                    val packet3 = event.packet
                    val pos3 = Vec3d(packet3.x + Random.nextDouble(), packet3.y + Random.nextDouble() - 2.0, packet3.z + Random.nextDouble())
                    if (mc.player.getDistance(pos3.x, pos3.y, pos3.z) < 10.0 && explosionTimer.passed((explosionDelay * 1000.0f).toInt())) {
                        explosionTimer.reset()
                        popTexts.add(PopupText(superHeroTextsBlowup[Random.nextInt(superHeroTextsBlowup.size)], pos3))
                    }
                }
            }
        }
    }


    class PopupText(val displayName: String, var pos: Vec3d) {
        private val timer = Timer()
        private val startTime = System.currentTimeMillis()
        private var yIncrease = Random.nextDouble()
        var xIncrease = Random.nextFloat()
        var zIncrease = Random.nextFloat()
        private val duration = 1000.0 * TextPopper.duration
        var isMarked = false
        var color: ColorRGB

        fun update() {
            pos = pos.add(0.0, yIncrease, 0.0)
            val presentA = 1.0 - ((System.currentTimeMillis() - startTime) / duration).coerceAtMost(1.0).coerceAtLeast(0.0)
            color = color.alpha((presentA * 255.0).toInt())
            if (timer.passed(duration.toInt())) {
                isMarked = true
            }
        }

        init {
            val hue = floatArrayOf(System.currentTimeMillis() % (360 * 32) / (360f * 32) * 6)
            color = ColorUtils.hsbToRGB(hue[0], 1.0f, 1.0f)
            while (yIncrease > 0.025 || yIncrease < 0.011) {
                yIncrease = Random.nextDouble()
            }
            if (!xRotate) xIncrease = 0f
            if (!zRotate) zIncrease = 0f
            timer.reset()
        }
    }


    enum class Font {
        Osaka, Knight, Badaboom
    }

}