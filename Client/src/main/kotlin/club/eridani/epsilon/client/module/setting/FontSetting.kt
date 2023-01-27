package club.eridani.epsilon.client.module.setting

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.TimeUnit
import club.eridani.epsilon.client.util.threads.AsyncCachedValue
import java.awt.GraphicsEnvironment
import java.util.*

internal object FontSetting : Module(
    name = "Font",
    alias = arrayOf("Font", "Custom", "Text"),
    category = Category.Setting,
    description = "Setting of Font"
) {

    val font = setting("Font", CustomFont.Comfortable, "The font of UI")
    val sizeSetting = setting("Size", 1.0f, 0.5f..2.0f, 0.05f, "The font size")
    private val gapSetting = setting("Gap", 0.0f, -10f..10f, 0.5f, "The font gap")
    private val lineSpaceSetting = setting("LineSpace", 0.0f, -10f..10f, 0.05f, "The line space")
    private val baselineOffsetSetting = setting("BaselineOffset", 0.0f, -10.0f..10.0f, 0.05f, "The base line offset")
    private val lodBiasSetting = setting("LodBias", 0.0f, -10.0f..10.0f, 0.05f, "Lod bias value")

    val isDefaultFont get() = font.value == CustomFont.Comfortable
    val size get() = sizeSetting.value * 0.1425f
    val gap get() = gapSetting.value * 0.5f - 2.05f
    val lineSpace get() = size * (lineSpaceSetting.value * 0.05f + 0.77f)
    val lodBias get() = lodBiasSetting.value * 0.25f - 0.5375f
    val baselineOffset get() = baselineOffsetSetting.value * 2.0f - 4.5f

    val availableFonts: Map<String, String> by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        HashMap<String, String>().apply {
            val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()

            environment.availableFontFamilyNames.forEach {
                this[it.lowercase(Locale.ROOT)] = it
            }

            environment.allFonts.forEach {
                this[it.name.lowercase(Locale.ROOT)] = it.family
            }
        }
    }

    enum class CustomFont(val fontName: String) {
        Comfortable("Comfortable"),
        TCM("TCM")
    }

}