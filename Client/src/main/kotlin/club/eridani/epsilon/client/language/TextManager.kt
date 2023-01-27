package club.eridani.epsilon.client.language

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.module.setting.TextSetting
import java.io.File

@Suppress("NOTHING_TO_INLINE")
object TextManager {

    val registeredTexts = mutableListOf<TextUnit>()

    private val languages = listOf(
        InnerLanguage.English.standardName,
        InnerLanguage.Chinese.standardName,
        InnerLanguage.Russian.standardName,
        InnerLanguage.Japanese.standardName
    )

    private val currentLanguage: String
        get() {
            return if (TextSetting.first.value != TextSetting.Language.Custom) {
                TextSetting.first.value.standardName
            } else {
                TextSetting.customLanguage.value
            }
        }

    private val secondLanguage: String
        get() {
            return if (TextSetting.second.value != TextSetting.Language.Custom) {
                TextSetting.second.value.standardName
            } else {
                TextSetting.customLanguage.value
            }
        }

    fun setText() {
        registeredTexts.forEach {
            if (!it.set(currentLanguage)) {
                if (!it.set(secondLanguage)) {
                    it.set(InnerLanguage.English.standardName)
                }
            }
        }
    }

    fun readText() {
        languages.forEach {
            try {
                readLanguage(it)
            } catch (ignore: Exception) {
                saveLanguage(it)
            }
        }
    }

    fun saveText() {
        languages.forEach {
            saveLanguage(it)
        }
    }

    private inline fun mkdirs() {
        File(Epsilon.DEFAULT_CONFIG_PATH + "language").mkdirs()
    }

    private inline fun saveLanguage(standardName: String) {
        val file = File(Epsilon.DEFAULT_CONFIG_PATH + "language/$standardName.txt")
        if (!file.exists()) {
            mkdirs()
            file.createNewFile()
        }
        val text = mutableListOf<String>()
        registeredTexts.forEach {
            text.add(it.basicName + "=" + (it.values[standardName] ?: "") + "\n")
        }
        file.writeText(text.joinToString(separator = ""))
    }

    private inline fun readLanguage(standardName: String) {
        File(Epsilon.DEFAULT_CONFIG_PATH + "language/$standardName.txt").readLines().forEach { s ->
            s.split("=", limit = 2).let { v ->
                registeredTexts.find {
                    it.basicName == v[0]
                }?.values?.set(standardName, v.getOrElse(1) { "" })
            }
        }
    }

}