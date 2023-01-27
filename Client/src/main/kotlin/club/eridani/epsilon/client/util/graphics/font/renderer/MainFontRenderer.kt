package club.eridani.epsilon.client.util.graphics.font.renderer

import club.eridani.epsilon.client.module.setting.FontSetting
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ColorUtils
import club.eridani.epsilon.client.util.Logger
import club.eridani.epsilon.client.util.graphics.GlStateUtils
import java.awt.Font

object MainFontRenderer : IFontRenderer {

    private var delegate: ExtendedFontRenderer

    private val defaultFont: Font = Font("Microsoft YaHei UI Regular", Font.PLAIN, 64)
//    private val defaultFont: Font = Font.createFont(Font.TRUETYPE_FONT, MainFontRenderer.javaClass.getResourceAsStream("/assets/minecraft/fonts/sfuiregular.ttf")).deriveFont(64f).deriveFont(Font.PLAIN)

    init {
        delegate = loadFont()
    }

    fun reloadFonts() {
        delegate.destroy()
        delegate = loadFont()
    }

    private fun loadFont(): ExtendedFontRenderer {
        val font = try {
            if (FontSetting.isDefaultFont) {
                defaultFont
            } else {
                Font(FontSetting.font.value.fontName, Font.PLAIN, 64)
            }
        } catch (e: Exception) {
            Logger.warn("Failed loading main font. Using Sans Serif font.")
            e.printStackTrace()
            AbstractFontRenderer.getSansSerifFont()
        }

        return DelegateFontRenderer(font)
    }

    fun drawStringJava(string: String, posX: Float, posY: Float, color: Int, scale: Float, drawShadow: Boolean) {
        var adjustedColor = color
        if (adjustedColor and -67108864 == 0) adjustedColor = color or -16777216

        GlStateUtils.alpha(false)
        drawString(string, posX, posY - 1.0f, ColorRGB(ColorUtils.argbToRgba(adjustedColor)), scale, drawShadow)
        GlStateUtils.alpha(true)
    }

    fun drawCenteredString(string: String, posX: Float, posY: Float, color: ColorRGB, scale: Float, drawShadow: Boolean) {
        val width: Float = getWidth(string, scale) / 2
        delegate.drawString(string, posX - width, posY, color, scale, drawShadow)
    }

    override fun drawString(string: String, posX: Float, posY: Float, color: ColorRGB, scale: Float, drawShadow: Boolean, splitting: Boolean) {
        delegate.drawString(string, posX, posY, color, scale, drawShadow, splitting)
    }

    override fun getWidth(text: String, scale: Float): Float {
        return delegate.getWidth(text, scale)
    }

    override fun getWidth(char: Char, scale: Float): Float {
        return delegate.getWidth(char, scale)
    }

    override fun getHeight(scale: Float): Float {
        return delegate.run {
            regularGlyph.fontHeight * FontSetting.lineSpace * scale
        }
    }
}