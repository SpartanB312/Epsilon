package club.eridani.epsilon.client.module.setting

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.and
import club.eridani.epsilon.client.common.extensions.atValue
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.shaders.GLSLSandbox
import club.eridani.epsilon.client.util.graphics.texture.MipmapTexture
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.io.File
import javax.imageio.ImageIO

object MenuSetting : Module(
    name = "MainMenu",
    alias = arrayOf("Menu", "Shader"),
    category = Category.Setting,
    description = "Main menu setting"
) {

    init {
        this.enable()
    }

    private val background = setting("Background", Background.Shader, "Use shader or an image as background")

    private val mode = setting(
        "ShaderMode",
        ShaderMode.Random,
        "Use the specified shader or randomly",
        background.atValue(Background.Shader)
    )

    private val shader = setting(
        "Shader",
        Shader.GreenNebula,
        "Specify a shader",
        background.atValue(Background.Shader) and mode.atValue(ShaderMode.Specified)
    ).listen {
        if (Thread.currentThread() == Epsilon.mainThread) reset()
    }

    enum class Background {
        Shader,
        Image
    }

    enum class ShaderMode {
        Specified,
        Random
    }

    enum class Shader {
        PurpleNoise,
        BlueLandscape,
        RedLandscape,
        GreenNebula,
        Meteor,
        Rainbow,
        DayNightSwitches,
    }

    private val shaderCache = HashMap<String, GLSLSandbox>()
    private var initTime: Long = 0x22
    private var currentShader = getShader()

    fun drawBackground(mouseX: Int, mouseY: Int, gui: GuiScreen) {
        when (background.value) {
            Background.Shader -> renderShader()
            Background.Image -> renderImage(mouseX, mouseY, gui)
        }
    }

    private var currentX = 0f
    private var currentY = 0f
    private var translate: Translate? = null
    private var PHOTO_PATH = Epsilon.DEFAULT_CONFIG_PATH + "background/background.jpg"
    var texture: MipmapTexture? = null

    class Translate(var x: Float, var y: Float)

    init {
        try {
            var p = File(PHOTO_PATH)
            if (!p.exists()) {
                p = File(PHOTO_PATH.replace(".jpg", ".png"))
            }
            texture = if (p.exists()) {
                MipmapTexture(ImageIO.read(p), GL11.GL_RGBA, 3)
            } else {
                p.parentFile.mkdir()
                MipmapTexture(
                    ImageIO.read(MenuSetting::class.java.getResourceAsStream("/assets/minecraft/textures/gui/background.jpg")),
                    GL11.GL_RGBA,
                    3
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderImage(mouseX: Int, mouseY: Int, gui: GuiScreen) {
        var mouseY0 = mouseY
        val sr = ScaledResolution(mc)
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        GlStateManager.pushMatrix()
        val xDiff = (mouseX - sr.scaledWidth / 2f - currentX) / sr.scaleFactor
        val yDiff = (mouseY0 - sr.scaledHeight / 2f - currentY) / sr.scaleFactor
        mouseY0 += translate!!.y.toInt()
        val slide = translate!!.y
        currentX += xDiff * 0.3f
        currentY += yDiff * 0.3f
        GlStateManager.translate(currentX / 100, currentY / 100, 0f)
        texture!!.bindTexture()
        GuiScreen.drawScaledCustomSizeModalRect(
            -10,
            -10,
            0f,
            0f,
            w + 20,
            h + 20,
            w + 20,
            h + 20,
            (w + 20).toFloat(),
            (h + 20).toFloat()
        )
        texture!!.unbindTexture()
        RenderUtils2D.drawRectFilled(-10, -10, gui.width + 10, gui.height + 10, ColorRGB(0, 0, 0, 80))


        GlStateManager.translate(-currentX / 100, -currentY / 100, 0f)
        GlStateManager.translate(currentX / 50, currentY / 50 - slide, 0f)
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun renderShader() {
        val width = mc.displayWidth.toFloat()
        val height = mc.displayHeight.toFloat()
        val mouseX = Mouse.getX() - 1.0f
        val mouseY = height - Mouse.getY() - 1.0f

        currentShader.render(width, height, mouseX, mouseY, initTime)
    }

    @JvmStatic
    fun reset() {
        translate = Translate(0f, -10f)
        initTime = System.currentTimeMillis()
        currentShader = getShader()
    }

    private fun getShader(): GLSLSandbox {
        val shaderIn = if (mode.value == ShaderMode.Random) {
            Shader.values().random().name
        } else {
            shader.value.name
        }

        return shaderCache.getOrPut(shaderIn) {
//            Logger.info("/assets/minecraft/shaders/menu/$shaderIn.fsh")
            GLSLSandbox("/assets/minecraft/shaders/menu/$shaderIn.fsh")
        }
    }

}