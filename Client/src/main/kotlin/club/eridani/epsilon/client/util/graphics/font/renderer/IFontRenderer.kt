package club.eridani.epsilon.client.util.graphics.font.renderer

import club.eridani.epsilon.client.util.ColorRGB

interface IFontRenderer {
    fun drawString(
        string: String,
        posX: Float = 0.0f,
        posY: Float = 0.0f,
        color: ColorRGB,
        scale: Float = 1.0f,
        drawShadow: Boolean = false,
        splitting: Boolean = false
    )

    fun drawStringWithShadow(
        string: String,
        posX: Float = 0.0f,
        posY: Float = 0.0f,
        color: ColorRGB,
        scale: Float = 1.0f
    ) {
        drawString(string, posX, posY, color, scale, true)
    }

    fun getHeight(): Float {
        return getHeight(1.0f)
    }

    fun getHeight(scale: Float): Float

    fun getWidth(text: String): Float {
        return getWidth(text, 1.0f)
    }

    fun getWidth(text: String, scale: Float): Float

    fun getWidth(char: Char): Float {
        return getWidth(char, 1.0f)
    }

    fun getWidth(char: Char, scale: Float): Float
}