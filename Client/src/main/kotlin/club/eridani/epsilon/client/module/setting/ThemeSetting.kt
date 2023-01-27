package club.eridani.epsilon.client.module.setting

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.gui.def.ThemeContainer
import club.eridani.epsilon.client.module.Module

object ThemeSetting : Module(
    name = "Theme",
    alias = arrayOf("Theme", "Skin"),
    category = Category.Setting,
    description = "Change the theme of this client"
) {

    val theme = setting("Theme", Themes.Metro, "The theme of GUI").valueListen { _, input ->
        ThemeContainer.update(input)
    }

    enum class Themes {
        Metro,
        Hyper,
        Flat,
        Rainbow
    }

}