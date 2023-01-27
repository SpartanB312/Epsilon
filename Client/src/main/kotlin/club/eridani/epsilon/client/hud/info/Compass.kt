package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.TextureManager
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ScaleHelper
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.math.Vec2d
import kotlin.math.abs


object Compass :
    HUDModule(name = "Compass", category = Category.InfoHUD, description = "Display the direction you facing") {

    private val cpWidth = setting("Width", 200, 100..1920, 1)
    private val shadow by setting("Shadow", true)
    private val fontShadow by setting("Font Shadow", false)
    private val line by setting("Line", true)
    private val autoMiddle by setting("AutoMiddle", defaultValue = {
       resize {
           x = (ScaleHelper.width / 2f).toInt() - (width / 2f).toInt()
       }
    })

    init {
        resize {
            width = cpWidth.value
            height = 20
        }
        cpWidth.valueListen { _, input ->
            resize {
                width = input
                height = 20
            }
        }
    }

    private val directions = mutableListOf(
        Direction("N", 1),
        Direction("195", 2),
        Direction("210", 2),
        Direction("NE", 3),
        Direction("240", 2),
        Direction("255", 2),
        Direction("E", 1),
        Direction("285", 2),
        Direction("300", 2),
        Direction("SE", 3),
        Direction("330", 2),
        Direction("345", 2),
        Direction("S", 1),
        Direction("15", 2),
        Direction("30", 2),
        Direction("SW", 3),
        Direction("60", 2),
        Direction("75", 2),
        Direction("W", 1),
        Direction("105", 2),
        Direction("120", 2),
        Direction("NW", 3),
        Direction("150", 2),
        Direction("165", 2)
    )

    override fun onRender() {
        if (Utils.nullCheck()) return
        val playerYaw = mc.player.rotationYaw
        val screenCenter = width / 2.0f
        val posY = y.toFloat()
        val yaw = playerYaw % 360 * 2 + 360 * 3

        var count = 0.0
        if (shadow)
        TextureManager.renderTextShadow(x - 10, y - 10 + (MainFontRenderer.getHeight() / 1.6).toInt(), width + 10, height + 10)
        // Begin scissor area
        for (direction in directions) {
            val position = x + screenCenter + count * 30 - yaw
            var resetPosition: Double
            when (direction.type) {
                1 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction, 1.8f) / 2.0f
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color(resetPosition), 1.8f, drawShadow = fontShadow)
                }
                2 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction) / 2.0f
                    val color = color(resetPosition)
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color, drawShadow = fontShadow)
                    if (line) RenderUtils2D.drawLine(Vec2d(position , posY.toDouble() + MainFontRenderer.getHeight()), Vec2d(position, posY.toDouble() + MainFontRenderer.getHeight() + 5.0), 1f, color)
                }
                3 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction, 1.8f) / 2.0f
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color(resetPosition), 1.8f, drawShadow = fontShadow)
                }
            }
            count++
        }
        for (direction in directions) {
            val position = x + screenCenter + count * 30 - yaw
            var resetPosition: Double
            when (direction.type) {
                1 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction, 1.8f) / 2.0f
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color(resetPosition), 1.8f, drawShadow = fontShadow)
                }
                2 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction) / 2.0f
                    val color = color(resetPosition)
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color, drawShadow = fontShadow)
                    if (line) RenderUtils2D.drawLine(Vec2d(position , posY.toDouble() + MainFontRenderer.getHeight()), Vec2d(position, posY.toDouble() + MainFontRenderer.getHeight() + 5.0), 1f, color)
                }
                3 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction, 1.8f) / 2.0f
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color(resetPosition), 1.8f, drawShadow = fontShadow)
                }
            }
            count++
        }
        for (direction in directions) {
            val position = x + screenCenter + count * 30 - yaw
            var resetPosition: Double
            when (direction.type) {
                1 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction, 1.8f) / 2f
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color(resetPosition), 1.8f, drawShadow = fontShadow)
                }
                2 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction) / 2.5f
                    val color = color(resetPosition)
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color, drawShadow = fontShadow)
                    if (line) RenderUtils2D.drawLine(Vec2d(position , posY.toDouble() + MainFontRenderer.getHeight()), Vec2d(position, posY.toDouble() + MainFontRenderer.getHeight() + 5.0), 1f, color)
                }
                3 -> {
                    resetPosition = position - MainFontRenderer.getWidth(direction.direction, 1.8f) / 2f
                    MainFontRenderer.drawString(direction.direction, resetPosition.toFloat(), posY, color(resetPosition), 1.8f, drawShadow = fontShadow)
                }
            }
            count++
        }
    }


    fun color(offset: Double): ColorRGB {
        val diff = abs((width / 2.0f) - (offset - x)) * 1.8f
        val offs = 255.0f * (1.069f - abs(diff / (width / 2.0f * 1.8f)))
            .coerceAtLeast(0.0).coerceAtMost(1.0)
        return GUIManager.white.alpha(offs.toInt())
    }

    class Direction(val direction: String, val type: Int)
}