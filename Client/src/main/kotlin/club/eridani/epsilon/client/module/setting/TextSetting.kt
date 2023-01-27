package club.eridani.epsilon.client.module.setting

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.language.TextManager
import club.eridani.epsilon.client.module.Module

internal object TextSetting : Module(
    name = "Language",
    alias = arrayOf("Text", "Language", "Word"),
    category = Category.Setting,
    description = "Setting of Text"
) {

    val first = setting("1st Language", Language.English, "First Language").listen {
        TextManager.setText()
    }
    val second = setting("2nd Language", Language.English, "Second Language").listen {
        TextManager.setText()
    }
    val customLanguage = setting("CustomLanguage", "Default", "Language Name") {
        first.value == Language.Custom || second.value == Language.Custom
    }
    val reload = setting("ReloadText", description = "Reload language text", defaultValue = {
        TextManager.readText()
        TextManager.setText()
    })

    enum class Language(val standardName: String) {
        Custom("Custom"),
        English("English"),
        Chinese("Chinese"),
        Japanese("Japanese"),
        Russian("Russian")
    }

}