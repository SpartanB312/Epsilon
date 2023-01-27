package club.eridani.epsilon.client.util.graphics.render.render

import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.VertexHelper
import club.eridani.epsilon.client.util.graphics.render.RenderTask
import org.lwjgl.opengl.GL11.GL_LINES

class LineRenderTask(
    private val startX: Float,
    private val startY: Float,
    private val endX: Float,
    private val endY: Float,
    private val color: ColorRGB,
    private val color2: ColorRGB = color,
) : RenderTask {

    override fun onRender() {
        RenderUtils2D.prepareGl()

        VertexHelper.begin(GL_LINES)

        VertexHelper.put(startX.toDouble(), startY.toDouble(), color)
        VertexHelper.put(endX.toDouble(), endY.toDouble(), color2)

        VertexHelper.end()

        RenderUtils2D.releaseGl()
    }

}