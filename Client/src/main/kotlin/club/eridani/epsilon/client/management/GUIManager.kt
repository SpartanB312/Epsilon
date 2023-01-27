package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.config.ConfigManager
import club.eridani.epsilon.client.gui.SpartanGUI
import club.eridani.epsilon.client.gui.def.DefaultHUDEditorScreen
import club.eridani.epsilon.client.gui.def.DefaultRootScreen
import club.eridani.epsilon.client.module.setting.GuiSetting
import club.eridani.epsilon.client.util.ColorHSB
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.render.asyncRender
import club.eridani.epsilon.client.util.onRender2D

object GUIManager {
    private var hue = 0.01f

    val white = ColorRGB(255, 255, 255, 255)
    val black = ColorRGB(0, 0, 0, 255)
    val defaultGUI = SpartanGUI(name = "DefaultGUI", rootGUI = DefaultRootScreen, hudEditor = DefaultHUDEditorScreen).also { ConfigManager.register(it.config) }
    val isParticle get() = GuiSetting.backgroundEffect.value == GuiSetting.BackgroundEffect.Particle
    val isRainbow get() = GuiSetting.rainbow.value
    val isBlur get() = GuiSetting.background.value == GuiSetting.Background.Blur || GuiSetting.background.value == GuiSetting.Background.Both
    val isShadow get() = GuiSetting.background.value == GuiSetting.Background.Shadow || GuiSetting.background.value == GuiSetting.Background.Both
    private val firstGUIColor get() = GuiSetting.firstGuiColor.value
    val firstTextColor = GuiSetting.getTextColor()[0]
    val primaryTextColor = GuiSetting.getTextColor()[1]
    private val rainbowTimer = Timer()

    init {
        onRender2D {
            asyncRender {
                if (rainbowTimer.passed(10)) {
                    rainbowTimer.reset()
                    if (isRainbow) {
                        hue += GuiSetting.rainbowSpeed.value / 1000.0f
                        if (hue > 1.0f) --hue
                    }
                }
            }.render()
        }
    }

    private val rainbowColor: ColorRGB
        get() {
            return ColorHSB(hue, GuiSetting.saturation.value, GuiSetting.brightness.value).toRGB()
        }


    val firstColor: ColorRGB
        get() {
            return if (isRainbow) rainbowColor
            else firstGUIColor
        }

}