package club.eridani.epsilon.client.util.graphics.font.renderer

import club.eridani.epsilon.client.module.setting.FontSetting
import java.awt.Font

class DelegateFontRenderer(font: Font) : ExtendedFontRenderer(font, 64.0f, 2048) {
    override val sizeMultiplier: Float
        get() = FontSetting.size

    override val baselineOffset: Float
        get() = FontSetting.baselineOffset

    override val charGap: Float
        get() = FontSetting.gap

    override val lineSpace: Float
        get() = FontSetting.lineSpace

    override val lodBias: Float
        get() = FontSetting.lodBias

    override val shadowDist: Float
        get() = 5.0f
}