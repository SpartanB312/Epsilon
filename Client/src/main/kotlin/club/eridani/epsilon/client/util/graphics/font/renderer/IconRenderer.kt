package club.eridani.epsilon.client.util.graphics.font.renderer

import club.eridani.epsilon.client.module.setting.FontSetting
import club.eridani.epsilon.client.util.ColorRGB
import java.awt.Font


object IconRenderer : IFontRenderer {

    private val iconFont = Font.createFont(
        Font.TRUETYPE_FONT,
        this.javaClass.getResourceAsStream("/assets/minecraft/fonts/IconFont.ttf")
    )
    private val delegate = DelegateFontRenderer(iconFont)

    fun drawCenteredString(
        string: String,
        posX: Float,
        posY: Float,
        color: ColorRGB,
        scale: Float,
        drawShadow: Boolean
    ) {
        val width: Float = getWidth(string, scale) / 2
        delegate.drawString(string, posX - width, posY, color, scale, drawShadow)
    }

    override fun drawString(
        string: String,
        posX: Float,
        posY: Float,
        color: ColorRGB,
        scale: Float,
        drawShadow: Boolean,
        splitting: Boolean
    ) {
        delegate.drawString(string, posX, posY, color, scale, drawShadow)
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