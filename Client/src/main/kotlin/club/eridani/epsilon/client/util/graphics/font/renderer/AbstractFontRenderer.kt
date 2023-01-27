package club.eridani.epsilon.client.util.graphics.font.renderer

import club.eridani.epsilon.client.module.setting.FontSetting
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.graphics.GlStateUtils
import club.eridani.epsilon.client.util.graphics.MatrixUtils
import club.eridani.epsilon.client.util.graphics.font.RenderString
import club.eridani.epsilon.client.util.graphics.font.Style
import club.eridani.epsilon.client.util.graphics.font.glyph.FontGlyphs
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import java.awt.Font

abstract class AbstractFontRenderer(font: Font, size: Float, private val textureSize: Int) : IFontRenderer {
    abstract val renderContext: AbstractFontRenderContext

    protected open val sizeMultiplier
        get() = 1.0f

    protected open val baselineOffset
        get() = 0.0f

    protected open val charGap
        get() = 0.0f

    protected open val lineSpace
        get() = 1.0f

    protected open val lodBias
        get() = 0.0f

    protected open val shadowDist
        get() = 2.0f

    val regularGlyph = loadFont(font, size, Style.REGULAR)

    private var prevCharGap = Float.NaN
    private var prevLineSpace = Float.NaN
    private var prevShadowDist = Float.NaN

    private val renderStringMap = Object2ObjectOpenHashMap<String, RenderString>()

    private val cleanTimer = TickTimer()

    protected fun loadFont(font: Font, size: Float, style: Style): FontGlyphs {
        // Load fallback font
        val fallbackFont = try {
            getFallbackFont().deriveFont(style.styleConst, size)
        } catch (e: Exception) {
            getSansSerifFont().deriveFont(style.styleConst, size)
        }

        return FontGlyphs(style.ordinal, font.deriveFont(style.styleConst, size), fallbackFont, textureSize)
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
        if (string.contains(ChatUtil.SECTION_SIGN) && !splitting) {
            var startX = posX
            val list = string.split(ChatUtil.SECTION_SIGN)
            list.forEachIndexed { index, s ->
                startX += if (s.isNotEmpty()) {
                    if (index != 0) {
                        val finalString = s.substring(1)
                        drawString(finalString, startX, posY, getColor(s[0], color), scale, drawShadow, true)
                        getWidth(finalString, scale)
                    } else {
                        drawString(s, startX, posY, color, scale, drawShadow, true)
                        getWidth(s, scale)
                    }
                } else 0f
            }
            return
        }
        if (cleanTimer.tickAndReset(1000L)) {
            val current = System.currentTimeMillis()
            renderStringMap.values.removeIf {
                it.tryClean(current)
            }
        }

        if (prevCharGap != charGap || prevLineSpace != lineSpace || prevShadowDist != shadowDist) {
            clearStringCache()
            prevCharGap = charGap
            prevLineSpace = lineSpace
            prevShadowDist = shadowDist
        }

        val stringCache = renderStringMap.computeIfAbsent(string) {
            RenderString(it).build(this, charGap, lineSpace, shadowDist)
        }

        GlStateUtils.texture2d(true)
        GlStateUtils.blend(true)

        val modelView = MatrixUtils.loadModelViewMatrix().getMatrix()
            .translate(posX, posY, 0.0f)
            .scale(sizeMultiplier * scale, sizeMultiplier * scale, 1.0f)
            .translate(0.0f, baselineOffset, 0.0f)

        stringCache.render(modelView, color, drawShadow, lodBias)
    }

    private fun getColor(char: Char, color: ColorRGB): ColorRGB {
        return when (char) {
            '0' -> ColorRGB(0, 0, 0, color.a)
            '1' -> ColorRGB(0, 0, 170, color.a)
            '2' -> ColorRGB(0, 170, 0, color.a)
            '3' -> ColorRGB(0, 170, 170, color.a)
            '4' -> ColorRGB(170, 0, 0, color.a)
            '5' -> ColorRGB(170, 0, 170, color.a)
            '6' -> ColorRGB(250, 170, 0, color.a)
            '7' -> ColorRGB(170, 170, 170, color.a)
            '8' -> ColorRGB(85, 85, 85, color.a)
            '9' -> ColorRGB(85, 85, 255, color.a)
            'a' -> ColorRGB(85, 255, 85, color.a)
            'b' -> ColorRGB(85, 255, 255, color.a)
            'c' -> ColorRGB(255, 85, 85, color.a)
            'd' -> ColorRGB(255, 85, 255, color.a)
            'e' -> ColorRGB(255, 255, 85, color.a)
            'r' -> color
            else -> ColorRGB(255, 255, 255, color.a)
        }
    }

    override fun getHeight(scale: Float): Float {
        return regularGlyph.fontHeight * scale
    }

    override fun getWidth(text: String, scale: Float): Float {
        var width = 0.0f
        val context = renderContext

        for ((index, char) in text.withIndex()) {
            if (char == '\n') continue
            if (context.checkFormatCode(text, index, false)) continue
            width += regularGlyph.getCharInfo(char).width + charGap
        }

        return width * sizeMultiplier * scale
    }

    override fun getWidth(char: Char, scale: Float): Float {
        return (regularGlyph.getCharInfo(char).width + charGap) * sizeMultiplier * scale
    }

    open fun destroy() {
        clearStringCache()
        regularGlyph.destroy()
    }

    fun clearStringCache() {
        renderStringMap.values.forEach {
            it.destroy()
        }
        renderStringMap.clear()
    }

    companion object {
        fun getFallbackFont(): Font {
            return Font(fallbackFonts.firstOrNull { FontSetting.availableFonts.containsKey(it) }, Font.PLAIN, 64)
        }

        fun getSansSerifFont(): Font {
            return Font(Font.SANS_SERIF, Font.PLAIN, 64)
        }

        private val fallbackFonts = arrayOf(
            "microsoft yahei ui",
            "microsoft yahei",
            "noto sans jp",
            "noto sans cjk jp",
            "noto sans cjk jp",
            "noto sans cjk kr",
            "noto sans cjk sc",
            "noto sans cjk tc", // Noto Sans
            "source han sans",
            "source han sans hc",
            "source han sans sc",
            "source han sans tc",
            "source han sans k", // Source Sans
        )
    }
}