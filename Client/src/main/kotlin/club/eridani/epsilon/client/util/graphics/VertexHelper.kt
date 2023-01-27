package club.eridani.epsilon.client.util.graphics

import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ColorUtils.toColor
import club.eridani.epsilon.client.util.math.Vec2d
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.Vec3d

@Suppress("NOTHING_TO_INLINE")
object VertexHelper {
    val tessellator = Tessellator.getInstance()
    val buffer = tessellator.buffer

    inline fun begin(mode: Int) {
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
    }

    inline fun put(pos: Vec3d, color: Int) {
        put(pos.x, pos.y, pos.z, color.toColor())
    }

    inline fun put(pos: Vec3d, color: ColorRGB) {
        put(pos.x, pos.y, pos.z, color)
    }

    inline fun put(x: Double, y: Double, z: Double, color: ColorRGB) {
        buffer.pos(x, y, z).color(color.r, color.g, color.b, color.a).endVertex()
    }

    inline fun put(pos: Vec2d, color: ColorRGB) {
        put(pos.x, pos.y, color)
    }

    inline fun put(x: Int, y: Int, color: ColorRGB) {
        buffer.pos(x.toDouble(), y.toDouble(), 0.0).color(color.r, color.g, color.b, color.a).endVertex()
    }

    inline fun put(pos: Vec2d, color: Int) {
        put(pos.x, pos.y, color.toColor())
    }

    inline fun put(x: Double, y: Double, color: ColorRGB) {
        buffer.pos(x, y, 0.0).color(color.r, color.g, color.b, color.a).endVertex()
    }

    inline fun end() {
        tessellator.draw()
    }
}