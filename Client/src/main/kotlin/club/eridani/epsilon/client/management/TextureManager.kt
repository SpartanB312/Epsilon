package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.util.Wrapper.mc
import club.eridani.epsilon.client.util.graphics.texture.ImageUtil
import club.eridani.epsilon.client.util.graphics.texture.MipmapTexture
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

object TextureManager {

    val logoMipmapTexture = ImageUtil.loadInJarImage("/assets/epsilon/textures/logo.png")!!
    val textShadowImage = ImageUtil.loadInJarImage("/assets/epsilon/textures/textShadow.png")!!

    val panelTop = ImageUtil.loadInJarImage("/assets/epsilon/textures/paneltop.png")!!
    val panelBottom = ImageUtil.loadInJarImage("/assets/epsilon/textures/panelbottom.png")!!
    val panelLeft = ImageUtil.loadInJarImage("/assets/epsilon/textures/panelleft.png")!!
    val panelRight = ImageUtil.loadInJarImage("/assets/epsilon/textures/panelright.png")!!

    val panelTopLeft = ImageUtil.loadInJarImage("/assets/epsilon/textures/paneltopleft.png")!!
    val panelTopRight = ImageUtil.loadInJarImage("/assets/epsilon/textures/paneltopright.png")!!
    val panelBottomLeft = ImageUtil.loadInJarImage("/assets/epsilon/textures/panelbottomleft.png")!!
    val panelBottomRight = ImageUtil.loadInJarImage("/assets/epsilon/textures/panelbottomright.png")!!

    fun renderTextShadow(x: Int, y: Int, width: Int, height: Int) {
        renderImage(textShadowImage, x, y, width, height, inGame = false)
    }

    fun renderShadowRect(x: Int, y: Int, width: Int, height: Int, shadowWidth: Int) {
        //panelTopLeft
        renderImage(panelTopLeft, x - shadowWidth, y - shadowWidth, shadowWidth, shadowWidth, inGame = false)
        //panelTopRight
        renderImage(panelTopRight, x + width, y - shadowWidth, shadowWidth, shadowWidth, inGame = false)
        //panelBottomLeft
        renderImage(panelBottomLeft, x - shadowWidth, y + height, shadowWidth, shadowWidth, inGame = false)
        //panelBottomRight
        renderImage(panelBottomRight, x + width, y + height, shadowWidth, shadowWidth, inGame = false)
        //panelTop
        renderImage(panelTop, x, y - 10, width, shadowWidth, inGame = false)
        //panelBottom
        renderImage(panelBottom, x, y + height, width, shadowWidth, inGame = false)
        //panelLeft
        renderImage(panelLeft, x - 10, y, shadowWidth, height, inGame = false)
        //panelRight
        renderImage(panelRight, x + width, y, shadowWidth, height, inGame = false)
    }

    fun renderPlayer2d(abstractClientPlayer: AbstractClientPlayer, x: Int, y: Int, width: Int, height: Int) {
        preRender()
        GlStateManager.color(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(abstractClientPlayer.locationSkin)
        GuiScreen.drawScaledCustomSizeModalRect(
            x, y,
            32f,
            32f,
            31,
            31,
            width,
            height,
            255f,
            255f
        )
        postRender()
    }


    fun renderImage(
        texture: MipmapTexture,
        posX: Int,
        posY: Int,
        width: Int,
        height: Int,
        textureX: Int = 0,
        textureY: Int = 0,
        inGame: Boolean = true
    ) {
        preRender()
        texture.bindTexture()
        if (inGame) mc.ingameGUI.drawTexturedModalRect(posX, posY, textureX, textureY, width, height)
        else GuiScreen.drawScaledCustomSizeModalRect(
            posX, posY,
            textureX.toFloat(),
            textureY.toFloat(),
            width,
            height,
            width,
            height,
            width.toFloat(),
            height.toFloat()
        )
        postRender()
    }

    private fun preRender() {
        GL11.glPushMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.clear(256)
        GlStateManager.enableBlend()
    }

    private fun postRender() {
        GlStateManager.disableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        GL11.glPopMatrix()
    }


}