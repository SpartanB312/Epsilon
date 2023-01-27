package club.eridani.epsilon.client.util.graphics.font.renderer

import club.eridani.epsilon.client.module.setting.FontSetting
import club.eridani.epsilon.client.util.ColorRGB
import java.awt.Font

class OtherFontRenderer(
    name: String,
    inJar: Boolean = true,
    private val syncMain: Boolean = false,
    size: Int = 64,
    textureSize: Int = 2048
) :
    ExtendedFontRenderer(
        if (inJar) Font.createFont(
            Font.TRUETYPE_FONT,
            MainFontRenderer.javaClass.getResourceAsStream("/assets/minecraft/fonts/$name.ttf")
        ).deriveFont(size.toFloat()) else Font(name, Font.PLAIN, size), size.toFloat(), textureSize
    ) {

    override val sizeMultiplier: Float
        get() = if (syncMain) FontSetting.size else super.sizeMultiplier

    override val baselineOffset: Float
        get() = if (syncMain) FontSetting.baselineOffset else super.baselineOffset

    override val charGap: Float
        get() = if (syncMain) FontSetting.gap else super.charGap

    override val lineSpace: Float
        get() = if (syncMain) FontSetting.lineSpace else super.lineSpace

    override val lodBias: Float
        get() = if (syncMain) FontSetting.lodBias else super.lodBias

    override val shadowDist: Float
        get() = if (syncMain) 5.0f else 2.0f

    fun drawCenteredString(
        string: String,
        posX: Float,
        posY: Float,
        color: ColorRGB,
        scale: Float,
        drawShadow: Boolean
    ) {
        val width: Float = getWidth(string, scale) / 2
        drawString(string, posX - width, posY, color, scale, drawShadow)
    }

}