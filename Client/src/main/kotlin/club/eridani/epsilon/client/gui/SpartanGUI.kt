package club.eridani.epsilon.client.gui

import club.eridani.epsilon.client.config.ConfigManager
import club.eridani.epsilon.client.config.GuiConfig
import club.eridani.epsilon.client.gui.def.AsyncRenderEngine
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

class SpartanGUI(val name: String, val rootGUI: SpartanScreen, val hudEditor: SpartanScreen) {
    val config = GuiConfig(this).also {
        ConfigManager.register(it)
    }
    private var animationWidth = 0f
    private var animationHeight = 0f
    private var animationAlpha = 0f

    var currentDescription: String? = null
    private var preDescription: String? = null
    val timer = Timer()

    fun drawDescription(engine: AsyncRenderEngine, mouseX: Int, mouseY: Int) {
        runSafe {
            with(engine) {


                    if (currentDescription != null) {
                        preDescription = currentDescription
                        val width = club.eridani.epsilon.client.management.Fonts.smallFont.getWidth(preDescription!!) / 8f + 2
                        val height = club.eridani.epsilon.client.management.Fonts.smallFont.getHeight() / 8f + 2
                        if (timer.passed(16)) {
                            timer.reset()
                            animationWidth = AnimationUtil.animate(width, animationWidth, 0.2f)
                            animationHeight = AnimationUtil.animate(height, animationHeight, 0.2f)
                            animationAlpha = AnimationUtil.animate(255f, animationAlpha, 0.15f)
                        }
                        currentDescription = null
                    } else {
                        if (timer.passed(16)) {
                            timer.reset()
                            animationWidth = AnimationUtil.animate(0f, animationWidth, 0.15f)
                            animationHeight = AnimationUtil.animate(0f, animationHeight, 0.15f)
                            animationAlpha = AnimationUtil.animate(0f, animationAlpha, 0.15f)
                        }
                    }


                if (animationAlpha == 0f) {
                    preDescription = null
                    return@runSafe
                }

                draw {
                    GlStateManager.enableDepth()
                    GlStateManager.depthMask(true)
                    GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT)
                    GlStateManager.depthFunc(GL11.GL_ALWAYS)
                }

                val color = if (club.eridani.epsilon.client.module.setting.GuiSetting.dynamicRainbow.value && club.eridani.epsilon.client.module.setting.GuiSetting.rainbow.value)
                    club.eridani.epsilon.client.util.ColorUtils.rainbowRGB(mouseY * -5, club.eridani.epsilon.client.module.setting.GuiSetting.saturation.value, club.eridani.epsilon.client.module.setting.GuiSetting.brightness.value)
                else
                    GUIManager.firstColor

                drawRect(
                    mouseX + 10f,
                    mouseY.toFloat(),
                    mouseX + 10f + animationWidth,
                    mouseY.toFloat() + animationHeight,
                    color.alpha(((animationAlpha / 255f) * 127).toInt())
                )

                draw { GlStateManager.depthFunc(GL11.GL_EQUAL) }
                drawString(preDescription!!,
                    mouseX + 11f,
                    mouseY + 1f,
                    GUIManager.white.alpha(animationAlpha.toInt()),
                    scale = 0.125F,
                    font = club.eridani.epsilon.client.management.Fonts.smallFont
                )
                draw { GlStateManager.depthFunc(GL11.GL_LEQUAL) }
            }
        }
    }
}