package club.eridani.epsilon.client.util.graphics

import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.OpenGlHelper.glUniformMatrix4
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11.*
import java.nio.FloatBuffer

@Suppress("NOTHING_TO_INLINE")
object MatrixUtils {
    val matrixBuffer: FloatBuffer = GLAllocation.createDirectFloatBuffer(16)

    inline fun loadProjectionMatrix(): MatrixUtils {
        matrixBuffer.clear()
        glGetFloat(GL_PROJECTION_MATRIX, matrixBuffer)
        return this
    }

    inline fun loadModelViewMatrix(): MatrixUtils {
        matrixBuffer.clear()
        glGetFloat(GL_MODELVIEW_MATRIX, matrixBuffer)
        return this
    }

    inline fun loadMatrix(matrix: Matrix4f): MatrixUtils {
        matrix.get(matrixBuffer)
        return this
    }

    inline fun getMatrix(): Matrix4f {
        return Matrix4f(matrixBuffer)
    }

    inline fun getMatrix(matrix: Matrix4f) {
        matrix.set(matrixBuffer)
    }

    inline fun uploadMatrix(location: Int) {
        glUniformMatrix4(location, false, matrixBuffer)
    }
}