package club.eridani.epsilon.client.module.setting

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import com.mojang.realmsclient.gui.ChatFormatting

object GuiSetting :
    Module(
        name = "Gui",
        alias = arrayOf("Theme", "Custom", "Style"),
        category = Category.Setting,
        description = "Setting of GUI"
    ) {

    val firstGuiColor = setting("1st GUIColor", ColorRGB(255, 0, 0, 255), "The first color of GUI")
    val asyncGUI by setting("AsyncUpdate", false)
    val unlimited by setting("Unlimited", false)
    val concurrentRefreshRate by setting("RefreshSpeed", 60, 30..500, 5) { !unlimited }

    val rainbow = setting("Rainbow", false, "Rainbow color effect")
    val dynamicRainbow = setting("DynamicRainbow", false) { rainbow.value }
    val rainbowSpeed = setting("RainbowSpeed", 1.0F, 0.0F..10.0F, 0.1F, "The speed of rainbow color") { rainbow.value }
    val brightness = setting("Brightness", 1.0F, 0.0F..1.0F, 0.05F, "The brightness of rainbow color") { rainbow.value }
    val saturation = setting("Saturation", 0.8F, 0.0F..1.0F, 0.05F, "The saturation of rainbow color") { rainbow.value }
    val background = setting("Background", Background.Shadow, "The background of UI")
    val backgroundEffect = setting("BGEffect", BackgroundEffect.None, "The background effect of UI")
    val descriptions by setting("Description", true, "Show descriptions")
    val moving by setting("Moving", true, "The panels will move to your mouse while your mose is moving")
    val movingRate by setting("MovingRate", 0.01, 0.005..0.1, 0.005, "The max moving rate") { moving }
    val unlimitedLength by setting("UnlimitedLength", false, "Unlimited Gui length")
    val maxLength by setting("MaxLength", 300, 100..800, 10, "The max length of Gui") { !unlimitedLength }
    val autoAdjust by setting("AutoAdjust", true, "Automatically adjust offset")
    val prefix = setting("Prefix", ".") { false }

    val textColor = setting("TextColor", TextColor.Gray)

    fun getTextColor(): Array<String> {
        return when (textColor.value) {
            TextColor.Red -> arrayOf(ChatFormatting.RED.toString(), ChatFormatting.DARK_RED.toString())
            TextColor.Green -> arrayOf(ChatFormatting.GREEN.toString(), ChatFormatting.DARK_GREEN.toString())
            TextColor.DarkGreen -> arrayOf(ChatFormatting.DARK_GREEN.toString(), ChatFormatting.GREEN.toString())
            TextColor.DarkRed -> arrayOf(ChatFormatting.DARK_RED.toString(), ChatFormatting.RED.toString())
            TextColor.Aqua -> arrayOf(ChatFormatting.DARK_AQUA.toString(), ChatFormatting.AQUA.toString())
            TextColor.Blue -> arrayOf(ChatFormatting.AQUA.toString(), ChatFormatting.DARK_AQUA.toString())
            TextColor.Gold -> arrayOf(ChatFormatting.GOLD.toString(), ChatFormatting.YELLOW.toString())
            TextColor.Yellow -> arrayOf(ChatFormatting.YELLOW.toString(), ChatFormatting.GOLD.toString())
            TextColor.DarkBlue -> arrayOf(ChatFormatting.DARK_BLUE.toString(), ChatFormatting.BLUE.toString())
            TextColor.BlueExtraDark -> arrayOf(ChatFormatting.BLUE.toString(), ChatFormatting.DARK_BLUE.toString())
            TextColor.Pink -> arrayOf(ChatFormatting.LIGHT_PURPLE.toString(), ChatFormatting.DARK_PURPLE.toString())
            TextColor.DarkPurple -> arrayOf(
                ChatFormatting.DARK_PURPLE.toString(),
                ChatFormatting.LIGHT_PURPLE.toString()
            )
            TextColor.White -> arrayOf(ChatFormatting.WHITE.toString(), ChatFormatting.BLACK.toString())
            TextColor.Gray -> arrayOf(ChatFormatting.GRAY.toString(), ChatFormatting.DARK_GRAY.toString())
            TextColor.DarkGray -> arrayOf(ChatFormatting.DARK_GRAY.toString(), ChatFormatting.GRAY.toString())
            else -> arrayOf(ChatFormatting.BLACK.toString(), ChatFormatting.WHITE.toString())
        }
    }

    enum class Background {
        Shadow, Blur, Both, None,
    }

    enum class BackgroundEffect {
        None, Particle
    }

    enum class TextColor {
        Red,
        Green,
        DarkGreen,
        DarkRed,
        Aqua,
        Blue,
        Gold,
        Yellow,
        DarkBlue,
        BlueExtraDark,
        Pink,
        DarkPurple,
        White,
        Gray,
        DarkGray,
        Black
    }

}