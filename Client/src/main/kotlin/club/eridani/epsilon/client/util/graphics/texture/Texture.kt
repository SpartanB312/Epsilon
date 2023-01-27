package club.eridani.epsilon.client.util.graphics.texture

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

open class Texture {

    var textureID = -1
    var width = 0
    var height = 0

    fun genTexture() {
        textureID = GL11.glGenTextures()
    }

    fun bindTexture() {
        if (textureID != -1) {
            GlStateManager.bindTexture(textureID)
        }
    }

    fun unbindTexture() {
        GlStateManager.bindTexture(0)
    }

    fun deleteTexture() {
        if (textureID != -1) {
            GlStateManager.deleteTexture(textureID)
            textureID = -1
        }
    }

}