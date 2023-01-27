package club.eridani.epsilon.client.util.graphics.render.render

import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.VertexHelper
import club.eridani.epsilon.client.util.graphics.render.RenderTask
import org.lwjgl.opengl.GL11

class RectRenderTask(
    private val x: Float,
    private val y: Float,
    private val endX: Float,
    private val endY: Float,
    private val color1: ColorRGB,
    private val color2: ColorRGB = color1,
    private val color3: ColorRGB = color1,
    private val color4: ColorRGB = color1,
    private val filled: Boolean = true
) : RenderTask {

    override fun onRender() {
        if (filled) RenderUtils2D.drawGradientRect(x, y, endX, endY, color1, color2, color3, color4)
        else {
            RenderUtils2D.prepareGl()

            GL11.glLineWidth(1f)

            VertexHelper.begin(GL11.GL_LINES)
            VertexHelper.put(endX.toDouble(), y.toDouble(), GUIManager.firstColor)
            VertexHelper.put(x.toDouble(), y.toDouble(), GUIManager.firstColor)

            VertexHelper.put(x.toDouble(), y.toDouble(), GUIManager.firstColor)
            VertexHelper.put(x.toDouble(), endY.toDouble(), GUIManager.firstColor)

            VertexHelper.put(x.toDouble(), endY.toDouble(), GUIManager.firstColor)
            VertexHelper.put(endX.toDouble(), endY.toDouble(), GUIManager.firstColor)

            VertexHelper.put(endX.toDouble(), endY.toDouble(), GUIManager.firstColor)
            VertexHelper.put(endX.toDouble(), y.toDouble(), GUIManager.firstColor)
            VertexHelper.end()

            RenderUtils2D.releaseGl()
        }
    }

}