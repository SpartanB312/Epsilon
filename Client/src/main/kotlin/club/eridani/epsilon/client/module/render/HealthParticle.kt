package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.graphics.font.renderer.IFontRenderer
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.math.MathUtils
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import kotlin.math.absoluteValue
import kotlin.random.Random


object HealthParticle : Module(name = "HealthParticle", alias = arrayOf("DamageParticle"), category = Category.Render, description = "Pop up players damage health") {

    private val players by setting("Player", true)
    private val self by setting("Self", false)
    private val animals by setting("Animal", false)
    private val mobs by setting("Mobs", true)
    val ticks by setting("ExistTick", 20, 1..100, 1)

    private val datas = mutableListOf<Data>()
    private val entityLastHealth = mutableMapOf<EntityLivingBase, Float>()

    init {
        onRender3D { it ->
            runSafe {
                if (mc.player.ticksExisted <= 10) return@runSafe
                if (datas.isNotEmpty()) {
                    for (data in datas) {
                        val pos = MathUtils.getInterpolateVec3dPos(Vec3d(data.x, data.y, data.z), it.partialTicks)
                        drawNameplate(MainFontRenderer, String.format("%.1f", data.data), pos.x, pos.y, pos.z, data.color)
                    }
                    datas.forEach {
                        it.update()
                    }
                }
                if (datas.isNotEmpty()) {
                    datas.removeIf { it.existTicks <= 0 }
                }
            }
        }

        onTick {
            runSafe {
                if (entityLastHealth.isNotEmpty()) {
                    entityLastHealth.forEach { (e, lasthealth) ->
                        if (mc.player.getDistance(e) <= 10) {
                            if (lasthealth != e.relativeHealth) {
                                val data = e.relativeHealth - lasthealth
                                if (data.absoluteValue >= 0.09) datas.add(Data(e.posX, e.posY, e.posZ, data))
                            }
                        }
                    }
                }
                entityLastHealth.clear()

                if (mc.world.loadedEntityList.isNotEmpty()) for (e in mc.world.loadedEntityList) {
                    if (e is EntityLivingBase) {
                        if (!self && mc.player == e) continue
                        if (!isValidEntity(e)) continue
                        entityLastHealth[e] = e.relativeHealth
                    }
                }
                datas.forEach {
                    it.existTicks--
                }
            }
        }
    }

    private fun drawNameplate(fontRendererIn: IFontRenderer, str: String, x: Double, y: Double, z: Double, color: ColorRGB) {
        val renderManager = mc.renderManager
        val f = 1.6f
        val f1 = 0.016666668f * f
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, z)
        GL11.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-f1, -f1, f1)
        RenderUtils3D.glPre(1f)
        fontRendererIn.drawStringWithShadow(str, -fontRendererIn.getWidth(str) / 2, 0f, color)
        RenderUtils3D.glPost()
        GlStateManager.popMatrix()
    }

    private fun isValidEntity(entity: Entity): Boolean {
        return entity is EntityLivingBase && players && entity is EntityPlayer || if (EntityUtil.isPassive(
                entity
            )
        ) animals else mobs
    }

    class Data(var x: Double, var y: Double, var z: Double, var data: Float) {
        var existTicks = 0
        var color: ColorRGB
        private var yIncrease = 0.0

        init {
            yIncrease = Random.nextDouble()
            existTicks = ticks
            while (yIncrease > 0.025 || yIncrease < 0.011) {
                yIncrease = Random.nextDouble()
            }
            y += yIncrease
            x += Random.nextDouble().coerceAtMost(0.5)
            z += Random.nextDouble().coerceAtMost(0.5)
            color = if (data > 0) {
                ColorRGB(0, 255, 0)
            } else {
                if (Random.nextBoolean()) ColorRGB(255, 0, 0) else ColorRGB(255, 255, 0)
            }
        }

        fun update() {
            y += 0.03
        }
    }
}