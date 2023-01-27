package club.eridani.epsilon.client.language

class TextUnit(val basicName: String, text: String) {

    val values = HashMap<String, String>()
    var currentText: String = values[InnerLanguage.English.standardName] ?: ""

    init {
        TextManager.registeredTexts.add(this)
        this.values[InnerLanguage.English.standardName] = text
    }

    fun add(language: InnerLanguage, description: String) {
        this.values[language.standardName] = description
    }

    fun set(languageName: String): Boolean {
        val text = values[languageName]
        if (text != null && text != "") {
            currentText = text
            return true
        }
        return false
    }

}
