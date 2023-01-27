package club.eridani.epsilon.client.util.graphics

import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ColorUtils
import club.eridani.epsilon.client.util.ScaleHelper
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.math.MathUtils
import club.eridani.epsilon.client.util.math.Vec2d
import club.eridani.epsilon.client.util.math.Vec2f
import club.eridani.epsilon.client.util.math.toRadian
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.*
import kotlin.math.*


/**
 * Utils for basic 2D shapes rendering
 */
@Suppress("NOTHING_TO_INLINE")
object RenderUtils2D {
    val mc = Wrapper.mc

    fun drawCircleOutline(center: Vec2f = Vec2f.ZERO, radius: Float, segments: Int = 0, lineWidth: Float = 1f, color: ColorRGB) {
        drawArcOutline(center, radius, Pair(0f, 360f), segments, lineWidth, color)
    }

    fun drawArcOutline(center: Vec2f = Vec2f.ZERO, radius: Float, angleRange: Pair<Float, Float>, segments: Int = 0, lineWidth: Float = 1f, color: ColorRGB) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawLineStrip(arcVertices, lineWidth, color)
    }

    private fun getArcVertices(center: Vec2f, radius: Float, angleRange: Pair<Float, Float>, segments: Int): Array<Vec2f> {
        val range = max(angleRange.first, angleRange.second) - min(angleRange.first, angleRange.second)
        val seg = calcSegments(segments, radius, range)
        val segAngle = (range / seg.toFloat())

        return Array(seg + 1) {
            val angle = (it * segAngle + angleRange.first).toRadian()
            val unRounded = Vec2f(sin(angle), -cos(angle)).times(radius).plus(center)
            Vec2f(MathUtils.round(unRounded.x, 8), MathUtils.round(unRounded.y, 8))
        }
    }

    private fun calcSegments(segmentsIn: Int, radius: Float, range: Float): Int {
        if (segmentsIn != -0) return segmentsIn
        val segments = radius * 0.5 * PI * (range / 360.0)
        return max(segments.roundToInt(), 16)
    }

    inline fun drawItem(itemStack: ItemStack, x: Int, y: Int, text: String? = null, drawOverlay: Boolean = true) {
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        RenderHelper.enableGUIStandardItemLighting()

        mc.renderItem.zLevel = 0.0f
        mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x, y)
        if (drawOverlay) mc.renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, text)
        mc.renderItem.zLevel = 0.0f

        RenderHelper.disableStandardItemLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        GlStateUtils.depth(false)
        GlStateUtils.texture2d(true)
    }

    inline fun drawGradientRect(
        x: Float,
        y: Float,
        endX: Float,
        endY: Float,
        color1: ColorRGB,
        color2: ColorRGB,
        color3: ColorRGB,
        color4: ColorRGB
    ) {
        GlStateUtils.texture2d(false)
        GlStateUtils.blend(true)
        GlStateUtils.alpha(false)
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateUtils.smooth(true)

        VertexHelper.begin(GL_QUADS)

        VertexHelper.put(endX.toDouble(), y.toDouble(), color1) //右上
        VertexHelper.put(x.toDouble(), y.toDouble(), color2) //左上
        VertexHelper.put(x.toDouble(), endY.toDouble(), color3) //左下
        VertexHelper.put(endX.toDouble(), endY.toDouble(), color4) //右下

        VertexHelper.end()

        GlStateUtils.blend(false)
        GlStateUtils.alpha(true)
        GlStateUtils.texture2d(true)
    }

    inline fun drawArcOutline(
        center: Vec2d = Vec2d(0.0, 0.0),
        radius: Double,
        angleRange: Pair<Float, Float>,
        segments: Int = 0,
        lineWidth: Float = 1f,
        color: ColorRGB
    ) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawLineStrip(arcVertices, lineWidth, color)
    }

    inline fun drawArcFilled(
        center: Vec2d = Vec2d(0.0, 0.0),
        radius: Double,
        angleRange: Pair<Float, Float>,
        segments: Int = 0,
        color: ColorRGB
    ) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawTriangleFan(center, arcVertices, color)
    }

    inline fun drawBorderedRect(x: Float, y: Float, endX: Float, endY: Float, lineWidth: Float, colorLine: ColorRGB, colorRect: ColorRGB) {
        drawRectOutline(x, y, endX, endY, colorLine, lineWidth)
        drawRectFilled(x, y, endX, endY, colorRect)
    }

    inline fun drawRectOutline(x: Float, y: Float, endX: Float, endY: Float, color: ColorRGB, lineWidth: Float = 1F) {
        prepareGl()
        glLineWidth(lineWidth)

        VertexHelper.begin(GL_LINE_LOOP)

        VertexHelper.put(x.toDouble(), y.toDouble(), color)
        VertexHelper.put(endX.toDouble(), y.toDouble(), color)
        VertexHelper.put(endX.toDouble(), endY.toDouble(), color)
        VertexHelper.put(x.toDouble(), endY.toDouble(), color)

        VertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    inline fun drawRectOutline(
        posBegin: Vec2d = Vec2d(0.0, 0.0),
        posEnd: Vec2d,
        lineWidth: Float = 1f,
        color: ColorRGB
    ) {
        val pos2 = Vec2d(posEnd.x, posBegin.y) // Top right
        val pos4 = Vec2d(posBegin.x, posEnd.y) // Bottom left
        val vertices = arrayOf(posBegin, pos2, posEnd, pos4)
        drawLineLoop(vertices, lineWidth, color)
    }

    fun drawOutline(x: Double, y: Double, width: Double, height: Double, lineWidth: Double, color: ColorRGB) {
        drawRectFilled(x, y, x + width, y + lineWidth, color)
        drawRectFilled(x, y, x + lineWidth, y + height, color)
        drawRectFilled(x, y + height - lineWidth, x + width, y + height, color)
        drawRectFilled(x + width - lineWidth, y, x + width, y + height, color)
    }

    inline fun drawRectFilled(x: Double, y: Double, endX: Double, endY: Double, color: ColorRGB) {
        val pos1 = Vec2d(x, y.toDouble())
        val pos2 = Vec2d(endX, y) // Top right
        val pos3 = Vec2d(endX, endY)
        val pos4 = Vec2d(x, endY) // Bottom left
        drawQuad(pos1, pos2, pos3, pos4, color)
    }

    inline fun drawRectFilled(x: Float, y: Float, endX: Float, endY: Float, color: ColorRGB) {
        val pos1 = Vec2d(x.toDouble(), y.toDouble())
        val pos2 = Vec2d(endX.toDouble(), y.toDouble()) // Top right
        val pos3 = Vec2d(endX.toDouble(), endY.toDouble())
        val pos4 = Vec2d(x.toDouble(), endY.toDouble()) // Bottom left
        drawQuad(pos1, pos2, pos3, pos4, color)
    }

    fun drawHsbColoredRect(x: Float, y: Float, endX: Float, endY: Float) {
        prepareGl()
        var lastOffset = 0f
        glBegin(GL_QUADS)
        for (offset in x.toInt()..endX.toInt()) {
            val color = ColorUtils.hsbToRGB(1 - (endX - offset) / (endX - x), 1f, 1f)

            glColor4f(color.rFloat, color.gFloat, color.bFloat, color.aFloat)
            if (lastOffset != 0f) glVertex2f(lastOffset, y)
            else glVertex2f(x, y)

            glVertex2f(offset.toFloat(), y)
            glVertex2f(offset.toFloat(), endY)

            if (lastOffset != 0f) glVertex2f(lastOffset, endY)
            else glVertex2f(x, endY)
            lastOffset = offset.toFloat()
        }
        glEnd()
        releaseGl()
    }

    inline fun drawRectFilled(x: Int, y: Int, endX: Int, endY: Int, color: ColorRGB) {
        val pos1 = Vec2d(x.toDouble(), y.toDouble())
        val pos2 = Vec2d(endX.toDouble(), y.toDouble()) // Top right
        val pos3 = Vec2d(endX.toDouble(), endY.toDouble())
        val pos4 = Vec2d(x.toDouble(), endY.toDouble()) // Bottom left
        drawQuad(pos1, pos2, pos3, pos4, color)
    }

    inline fun drawRectFilled(posBegin: Vec2d = Vec2d(0.0, 0.0), posEnd: Vec2d, color: ColorRGB) {
        val pos2 = Vec2d(posEnd.x, posBegin.y) // Top right
        val pos4 = Vec2d(posBegin.x, posEnd.y) // Bottom left
        drawQuad(posBegin, pos2, posEnd, pos4, color)
    }

    inline fun drawQuad(pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, pos4: Vec2d, color: ColorRGB) {
        val vertices = arrayOf(pos1, pos2, pos4, pos3)
        drawTriangleStrip(vertices, color)
    }

    inline fun drawQuad(pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, pos4: Vec2d, color: Int) {
        val vertices = arrayOf(pos1, pos2, pos4, pos3)
        drawTriangleStrip(vertices, color)
    }

    inline fun drawTriangleOutline(pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, lineWidth: Float = 1f, color: ColorRGB) {
        val vertices = arrayOf(pos1, pos2, pos3)
        drawLineLoop(vertices, lineWidth, color)
    }

    inline fun drawTriangleFilled(pos1: Vec2d, pos2: Vec2d, pos3: Vec2d, color: ColorRGB) {
        prepareGl()

        VertexHelper.begin(GL_TRIANGLES)
        VertexHelper.put(pos1, color)
        VertexHelper.put(pos2, color)
        VertexHelper.put(pos3, color)
        VertexHelper.end()

        releaseGl()
    }


    fun drawOutlineCircle(cx: Float, cy: Float, r: Float, color: ColorRGB) {
        prepareGl()

        VertexHelper.begin(GL_LINE_STRIP)
        for (i in 0..360) {
            val theta = (2 * PI).toFloat() * i / 360
            val x = r.toDouble() * cos(theta)
            val y = r.toDouble() * sin(theta)
            VertexHelper.put(Vec2d(x + cx, y + cy), color)
        }
        VertexHelper.end()

        releaseGl()
    }

    fun drawFilledCircle(x: Double, y: Double, r: Double, color: ColorRGB) {
        prepareGl()
        glColor4f(color.rFloat, color.gFloat, color.bFloat, color.aFloat)

        glEnable(GL_POINT_SMOOTH)
        glHint(GL_POINT_SMOOTH_HINT, GL_NICEST)

        glPointSize(r.toFloat() * ScaleHelper.lastScale * 2f)
        glBegin(GL_POINTS)
        glVertex2d(x, y)
        glEnd()

        glDisable(GL_POINT_SMOOTH)

        glColor4f(1f, 1f, 1f, 1f)
        releaseGl()
    }

    private fun fill(x: Double, y: Double, w: Double, h: Double, color: Int) {
        var x1 = x
        var y1 = y
        var x2 = x + w
        var y2 = y + h
        var j: Double
        if (x1 < x2) {
            j = x1
            x1 = x2
            x2 = j
        }
        if (y1 < y2) {
            j = y1
            y1 = y2
            y2 = j
        }
        val f = (color shr 24 and 255).toFloat() / 255.0f
        val g = (color shr 16 and 255).toFloat() / 255.0f
        val h = (color shr 8 and 255).toFloat() / 255.0f
        val k = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        prepareGl()
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferBuilder.pos(x1, y2, 0.0).color(g, h, k, f).endVertex()
        bufferBuilder.pos(x2, y2, 0.0).color(g, h, k, f).endVertex()
        bufferBuilder.pos(x2, y1, 0.0).color(g, h, k, f).endVertex()
        bufferBuilder.pos(x1, y1, 0.0).color(g, h, k, f).endVertex()
        tessellator.draw()
        releaseGl()
    }

    fun drawRoundedRectangle(x: Double, y: Double, w: Double, h: Double, radius: Double, color: ColorRGB) {
        //Cornors
        drawFilledCircle(x + radius, y + radius, radius, color)
        drawFilledCircle(x + w - radius, y + radius, radius, color)
        drawFilledCircle(x + radius, y + h - radius, radius, color)
        drawFilledCircle(x + w - radius, y + h - radius, radius, color)

        fill(x, y + radius, w, h - (2 * radius), color.toArgb())
        fill(x + radius, y, w - (2 * radius), h, color.toArgb())
    }

    inline fun drawTriangleFan(center: Vec2d, vertices: Array<Vec2d>, color: ColorRGB) {
        prepareGl()

        VertexHelper.begin(GL_TRIANGLE_FAN)
        VertexHelper.put(center, color)
        for (vertex in vertices) {
            VertexHelper.put(vertex, color)
        }
        VertexHelper.end()

        releaseGl()
    }

    inline fun drawHorizontalRect(x: Int, y: Int, endX: Int, endY: Int, color1: ColorRGB, color2: ColorRGB) {
        prepareGl()

        VertexHelper.begin(GL_TRIANGLE_STRIP)
        VertexHelper.put(x.toDouble(), y.toDouble(), color1) //左上
        VertexHelper.put(endX.toDouble(), y.toDouble(), color2) //右上
        VertexHelper.put(endX.toDouble(), endY.toDouble(), color2) //右下
        VertexHelper.put(x.toDouble(), endY.toDouble(), color1) //左下
        VertexHelper.end()

        releaseGl()
    }

    inline fun drawRect(
        x: Float,
        y: Float,
        endX: Float,
        endY: Float,
        color1: ColorRGB,
        color2: ColorRGB,
        color3: ColorRGB,
        color4: ColorRGB
    ) {
        prepareGl()

        VertexHelper.begin(GL_QUADS)
        VertexHelper.put(x.toDouble(), y.toDouble(), color1) //左上
        VertexHelper.put(endX.toDouble(), y.toDouble(), color2) //右上
        VertexHelper.put(endX.toDouble(), endY.toDouble(), color3) //右下
        VertexHelper.put(x.toDouble(), endY.toDouble(), color4) //左下
        VertexHelper.end()

        releaseGl()
    }

    inline fun drawTriangleStrip(vertices: Array<Vec2d>, color: ColorRGB) {
        prepareGl()

        VertexHelper.begin(GL_TRIANGLE_STRIP)
        for (vertex in vertices) {
            VertexHelper.put(vertex, color)
        }
        VertexHelper.end()

        releaseGl()
    }

    inline fun drawTriangleStrip(vertices: Array<Vec2d>, color: Int) {
        prepareGl()

        VertexHelper.begin(GL_TRIANGLE_STRIP)
        for (vertex in vertices) {
            VertexHelper.put(vertex, color)
        }
        VertexHelper.end()

        releaseGl()
    }

    inline fun drawLineLoop(vertices: Array<Vec2d>, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        glLineWidth(lineWidth)

        VertexHelper.begin(GL_LINE_LOOP)
        for (vertex in vertices) {
            VertexHelper.put(vertex, color)
        }
        VertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    inline fun drawLineStrip(vertices: Array<Vec2d>, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        glLineWidth(lineWidth)

        VertexHelper.begin(GL_LINE_STRIP)
        for (vertex in vertices) {
            VertexHelper.put(vertex, color)
        }
        VertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    inline fun drawLineStrip(vertices: Array<Vec2f>, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        glLineWidth(lineWidth)

        VertexHelper.begin(GL_LINE_STRIP)
        for (vertex in vertices) {
            VertexHelper.put(vertex.x.toDouble(), vertex.y.toDouble(), color)
        }
        VertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    inline fun drawLine(posBegin: Vec2d, posEnd: Vec2d, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        glLineWidth(lineWidth)

        VertexHelper.begin(GL_LINES)
        VertexHelper.put(posBegin, color)
        VertexHelper.put(posEnd, color)
        VertexHelper.end()

        releaseGl()
        glLineWidth(1f)
    }

    inline fun getArcVertices(
        center: Vec2d,
        radius: Double,
        angleRange: Pair<Float, Float>,
        segments: Int
    ): Array<Vec2d> {
        val range = max(angleRange.first, angleRange.second) - min(angleRange.first, angleRange.second)
        val seg = calcSegments(segments, radius, range)
        val segAngle = (range.toDouble() / seg.toDouble())

        return Array(seg + 1) {
            val angle = Math.toRadians(it * segAngle + angleRange.first.toDouble())
            val unRounded = Vec2d(sin(angle), -cos(angle)).times(radius).plus(center)
            Vec2d(MathUtils.round(unRounded.x, 8), MathUtils.round(unRounded.y, 8))
        }
    }

    inline fun calcSegments(segmentsIn: Int, radius: Double, range: Float): Int {
        if (segmentsIn != -0) return segmentsIn
        val segments = radius * 0.5 * PI * (range / 360.0)
        return max(segments.roundToInt(), 16)
    }

    inline fun prepareGl() {
        GlStateUtils.texture2d(false)
        GlStateUtils.blend(true)
        GlStateUtils.smooth(true)
        GlStateUtils.lineSmooth(true)
        GlStateUtils.cull(false)
    }

    inline fun releaseGl() {
        GlStateUtils.texture2d(true)
        GlStateUtils.smooth(false)
        GlStateUtils.lineSmooth(false)
        GlStateUtils.cull(true)
    }

    inline fun glScissor(x: Int, y: Int, x1: Int, y1: Int, sr: ScaledResolution) {
        glScissor(
            (x * sr.scaleFactor),
            (Wrapper.mc.displayHeight - y1 * sr.scaleFactor),
            ((x1 - x) * sr.scaleFactor),
            ((y1 - y) * sr.scaleFactor)
        )
    }
}