package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.Fonts
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.SpartanCore
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ColorUtils
import club.eridani.epsilon.client.util.TpsCalculator.globalInfoPingValue
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.VertexHelper
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.asyncRender
import club.eridani.epsilon.client.util.text.ChatUtil
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11


object Logo : HUDModule(name = "Logo", category = Category.InfoHUD, description = "Sexy Epsilon Logo") {

    private val mode by setting("Mode", Mode.EpsilonSense)
    private val font by setting("Font", Font.Osaka) { mode != Mode.EpsilonSense }
    private val scale by setting("Scale", 0.6f, 0.1f..1f, 0.01f) { mode != Mode.EpsilonSense }
    private val biomes by setting("Biomes", true)
    private val ip by setting("IP", true)
    private val fps by setting("FPS", true)
    private val update by setting("UPS", true)
    private val ping by setting("Ping", true)

    init {
        resize {
            width = 0
            height = 0
        }
    }

    private val asyncRenderer = asyncRender {

        if (mc.player == null || mc.world == null) return@asyncRender


        val drawFont = when (font) {
            Font.Osaka -> Fonts.osaka
            Font.Knight -> Fonts.knight
            Font.Badaboom -> Fonts.badaboom
            Font.YaHei -> Fonts.logoFont
        }

        val watermark = "Epsilon"

        when (mode) {
            Mode.EpsilonSense -> {
                var message = ChatUtil.SECTION_SIGN + "fepsilon" + ChatUtil.SECTION_SIGN + "rsense" + ChatUtil.SECTION_SIGN + "f"
                if (biomes) message += " | " + mc.world.getBiome(mc.player.position).biomeName.lowercase()
                if (ip) message += " | " + if (mc.isSingleplayer) "singleplayer" else mc.currentServerData?.serverIP?.lowercase()
                if (fps) message += " | " + Minecraft.getDebugFPS() + "fps"
                if (update) message += " | " + SpartanCore.updates + "ups"
                if (ping) message += " | " + globalInfoPingValue() + "ms"


                val messageWidth = MainFontRenderer.getWidth(message, 0.8f)
                val messageHeight = MainFontRenderer.getHeight(0.8f)
                //bg
                drawRect(x - 3f, y - 3f, x + messageWidth + 5f, y + messageHeight + 5f, ColorRGB(60, 60, 60, 255))
                drawRect(x - 2f, y - 2f, x + messageWidth + 4f, y + messageHeight + 4f, ColorRGB(40, 40, 40, 255))
                drawRect(x - 1f, y - 1f, x + messageWidth + 3f, y + messageHeight + 3f, ColorRGB(60, 60, 60, 255))
                //background
                drawRect(x.toFloat(), y.toFloat(), x + messageWidth + 2f, y + messageHeight, ColorRGB(22, 22, 22, 255))

                val startX = x.toDouble()
                val startY = y + messageHeight.toDouble() + 1
                val step = (messageWidth.toDouble() + 2) / 6.0

                draw {
                    RenderUtils2D.prepareGl()

                    GL11.glLineWidth(2f)

                    VertexHelper.begin(GL11.GL_LINES)

                    val delay = -600

                    VertexHelper.put(startX + step * 0, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 0) else GUIManager.firstColor)
                    VertexHelper.put(startX + step * 1, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 1) else GUIManager.firstColor)

                    VertexHelper.put(startX + step * 1, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 1) else GUIManager.firstColor)
                    VertexHelper.put(startX + step * 2, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 2) else GUIManager.firstColor)

                    VertexHelper.put(startX + step * 2, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 2) else GUIManager.firstColor)
                    VertexHelper.put(startX + step * 3, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 3) else GUIManager.firstColor)

                    VertexHelper.put(startX + step * 3, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 3) else GUIManager.firstColor)
                    VertexHelper.put(startX + step * 4, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 4) else GUIManager.firstColor)

                    VertexHelper.put(startX + step * 4, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 4) else GUIManager.firstColor)
                    VertexHelper.put(startX + step * 5, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 5) else GUIManager.firstColor)

                    VertexHelper.put(startX + step * 5, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 5) else GUIManager.firstColor)
                    VertexHelper.put(startX + step * 6, startY, if (GUIManager.isRainbow) ColorUtils.rainbowRGB(delay * 6) else GUIManager.firstColor)


                    VertexHelper.end()

                    RenderUtils2D.releaseGl()
                }


                drawString(message, x + 1f, y.toFloat() - 1f, ColorRGB(185, 255, 0), 0.8f)

                resize {
                    width = messageWidth.toInt() + 4
                    height = messageHeight.toInt() + 3
                }
            }
            Mode.Epsilon -> {
                drawString(watermark, x.toFloat(), y.toFloat(), GUIManager.firstColor.alpha(255), scale, drawFont)
                resize {
                    width = drawFont.getWidth("Epsilon", scale).toInt() + 1
                    height = drawFont.getHeight(scale).toInt() + 3
                }
            }
            Mode.FirstLetter -> {
                drawString(watermark.substring(0..0) + ChatUtil.SECTION_SIGN + "f" + watermark.substring(1), x.toFloat(), y.toFloat(), GUIManager.firstColor.alpha(255), scale, drawFont)
                resize {
                    width = drawFont.getWidth(watermark, scale).toInt() + 1
                    height = drawFont.getHeight(scale).toInt() + 3
                }
            }
        }
    }

    override fun onRender() {
        asyncRenderer.render()
    }


    enum class Mode {
        Epsilon, FirstLetter, EpsilonSense
    }

    enum class Font {
        Osaka, Knight, Badaboom, YaHei
    }


}