package club.eridani.epsilon.client.util.graphics.texture

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL30
import java.awt.image.BufferedImage

class MipmapTexture(bufferedImage: BufferedImage, format: Int, levels: Int) : Texture() {
    init {
        width = bufferedImage.width
        height = bufferedImage.height
        genTexture()
        bindTexture()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, levels)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, levels)
        ImageUtil.uploadImage(bufferedImage, format, width, height)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)
        unbindTexture()
    }
}