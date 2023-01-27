package club.eridani.epsilon.client.util.graphics.texture

import net.minecraft.client.renderer.GLAllocation
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL14
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object ImageUtil {

    private val buffer = GLAllocation.createDirectIntBuffer(0x1000000)

    @Synchronized
    fun uploadImage(bufferedImage: BufferedImage, format: Int, width: Int, height: Int) {
        val data = IntArray(width * height)
        bufferedImage.getRGB(0, 0, width, height, data, 0, width)
        buffer.put(data)
        buffer.flip()
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            format,
            width,
            height,
            0,
            GL12.GL_BGRA,
            GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
            buffer
        )
        buffer.clear()
    }

    fun loadInJarImage(path: String, levels: Int = 3): MipmapTexture? {
        return try {
            MipmapTexture(
                ImageIO.read(ImageUtil::class.java.getResourceAsStream(path)),
                GL11.GL_RGBA,
                levels
            ).prepare()
        } catch (ignore: Exception) {
            null
        }
    }

    fun loadImage(path: String, levels: Int = 3): MipmapTexture? {
        return try {
            MipmapTexture(ImageIO.read(File(path)), GL11.GL_RGBA, levels).prepare()
        } catch (ignore: Exception) {
            null
        }
    }

    private fun MipmapTexture.prepare(): MipmapTexture {
        this.bindTexture()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.5f)
        this.unbindTexture()
        return this
    }

}