package club.eridani.epsilon.client.hud.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.font.renderer.IconRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.asyncRender
import club.eridani.epsilon.client.util.math.Vec2d
import club.eridani.epsilon.client.util.onPacketReceive
import club.eridani.epsilon.client.util.onTick
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.ItemPickaxe
import net.minecraft.network.play.server.SPacketBlockBreakAnim
import org.lwjgl.opengl.GL11
import kotlin.math.pow
import kotlin.math.sin

//TODO: NEED TEST
object ObsidianWarning :
    HUDModule(name = "ObsidianWarning", category = Category.CombatHUD, description = "Warn you when someone is mining obsidian near you") {
    private val minRange by setting("Obby Range", 1.5, 0.0..10.0, 0.1)
    private val timeOut by setting("Timeout Tick", 20, 0..100, 1)

    private var animationWidth = 0.0f
    private val animationTimer = Timer()
    private var startTime = -1L
    private var playerName: String? = null
    private var progress: String? = null
    private var warn = false
    private var delay = 0

    private val asSyncUpdateAnimate = asyncRender {
        val target: Float
        if (warn) {
            target = 150f
        } else {
            target = 0f
            startTime = -1L
        }
        if (animationTimer.passed(16)) {
            animationWidth = AnimationUtil.animate(target, animationWidth, 0.2f)
            animationTimer.reset()
        }
    }

    init {
        resize {
            width = 150
            height = 35
        }
        onPacketReceive { event ->
            if (event.packet is SPacketBlockBreakAnim) {
                val progress = event.packet.progress
                val breakerId = event.packet.breakerId
                val pos = event.packet.position ?: return@onPacketReceive
                val block = mc.world.getBlockState(pos).block
                val breaker = mc.world.getEntityByID(breakerId)
                if (breaker is EntityPlayer) {
                    if (block != Blocks.OBSIDIAN) return@onPacketReceive
                    if (breaker.heldItemMainhand.isEmpty || breaker.heldItemMainhand.item !is ItemPickaxe) return@onPacketReceive
                    if (mc.player.getDistanceSqToCenter(pos) <= minRange.pow(2.0)) {
                        playerName = breaker.name
                        if (progress == 255) ObsidianWarning.progress = "100%" else ObsidianWarning.progress = (progress * 9).toString() + "%"
                        warn = true
                        delay = 0
                        if (startTime == -1L) startTime = System.currentTimeMillis()
                        if (progress == 255) {
                            warn = false
                        }
                    }
                }
            }
        }
        onTick {
            if (delay++ > timeOut) warn = false
        }
    }

    override fun onRender() {
        asSyncUpdateAnimate.render()
        if (animationWidth > .2f) {
            val i = 0.coerceAtLeast(255.coerceAtMost((sin(startTime - System.currentTimeMillis() / 100.0) * 255.0 / 2 + 127.5).toInt()))
            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT)
            GlStateManager.depthFunc(GL11.GL_ALWAYS)
            RenderUtils2D.drawRectFilled(x.toFloat(), y.toFloat(), x + animationWidth, y + 35f, ColorRGB(0, 0, 0, 128))
            RenderUtils2D.drawRectFilled(x.toFloat(), y.toFloat(), x + animationWidth, y - 2f, ColorRGB(i, 0, 0))

            GlStateManager.depthFunc(GL11.GL_EQUAL)
            RenderUtils2D.drawLine(Vec2d(x + 30.0, y + MainFontRenderer.getHeight(0.8f) + 4.0), Vec2d(x + 148.0, y + MainFontRenderer.getHeight(0.8f) + 4.0), 1.5f, GUIManager.white.alpha(182))
            IconRenderer.drawString("e", x.toFloat() + 5, y.toFloat() + 3, GUIManager.white, 3.2f)
            MainFontRenderer.drawString("Obsidian Warning!", x + 30f, y.toFloat() + 2, GUIManager.white, 0.8f)
            MainFontRenderer.drawString("Player: $playerName", x + 30f, y.toFloat() + 16, GUIManager.white, 0.8f)
            MainFontRenderer.drawString("Progress: $progress", x + 30f, y.toFloat() + 24, GUIManager.white, 0.8f)
            GlStateManager.depthFunc(GL11.GL_LEQUAL)
        }
    }
}