package club.eridani.epsilon.client.util.graphics.render

import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.font.renderer.IFontRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.render.CircleRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.LineRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.RectRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.TextRenderTask

@Suppress("NOTHING_TO_INLINE")
abstract class RawAsyncRenderer {

    protected val tasks = mutableListOf<RenderTask>()
    val tempTasks = mutableListOf<RenderTask>()

    abstract fun update()

    fun render() {
        synchronized(tasks) {
            tasks.forEach {
                it.onRender()
            }
        }
    }

    inline fun draw(
        crossinline task: () -> Unit
    ) {
        tempTasks.add(object : RenderTask {
            override fun onRender() {
                task.invoke()
            }
        })
    }

    inline fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: ColorRGB,
        color2: ColorRGB = color1,
        color3: ColorRGB = color1,
        color4: ColorRGB = color1,
        filled: Boolean = true
    ) {
        tempTasks.add(RectRenderTask(x, y, width, height, color1, color2, color3, color4, filled))
    }

    inline fun drawString(
        text: String,
        x: Float,
        y: Float,
        colorRGB: ColorRGB,
        scale: Float = 1.0F,
        font: IFontRenderer = MainFontRenderer
    ) {
        tempTasks.add(TextRenderTask(text, x, y, colorRGB, scale, false, font))
    }

    inline fun drawStringWithShadow(
        text: String,
        x: Float,
        y: Float,
        colorRGB: ColorRGB,
        scale: Float = 1.0F,
        font: IFontRenderer = MainFontRenderer
    ) {
        tempTasks.add(TextRenderTask(text, x, y, colorRGB, scale, true, font))
    }

    inline fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: ColorRGB,
        color2: ColorRGB = color,
    ) {
        tempTasks.add(LineRenderTask(startX, startY, endX, endY, color, color2))
    }

    inline fun drawCircle(
        x: Float,
        y: Float,
        r: Float,
        color: ColorRGB,
        filled: Boolean = true
    ) {
        tempTasks.add(CircleRenderTask(x, y, r, color, filled))
    }

}