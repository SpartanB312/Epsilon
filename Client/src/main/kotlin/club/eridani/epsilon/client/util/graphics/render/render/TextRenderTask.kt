package club.eridani.epsilon.client.util.graphics.render.render

import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.font.renderer.IFontRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.RenderTask

class TextRenderTask(
    private val text: String,
    private val x: Float,
    private val y: Float,
    private val colorRGB: ColorRGB,
    private val scale: Float = 1.0F,
    private val isShadow: Boolean = true,
    private val font: IFontRenderer = MainFontRenderer
) : RenderTask {

    override fun onRender() {
        if (isShadow) font.drawStringWithShadow(text, x, y, colorRGB, scale)
        else font.drawString(text, x, y, colorRGB, scale)
    }

}
