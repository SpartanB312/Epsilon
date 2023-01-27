package club.eridani.epsilon.client.setting

import club.eridani.epsilon.client.language.InnerLanguage
import club.eridani.epsilon.client.language.TextUnit
import club.eridani.epsilon.client.management.SpartanCore
import java.util.*
import kotlin.reflect.KProperty

open class MutableSetting<T : Any>(
    final override val name: String,
    valueIn: T,
    moduleName: String = "",
    description: String = "",
    override val visibility: () -> Boolean,
) : AbstractSetting<T>() {

    override val description = TextUnit(
        "module_"
                + moduleName.lowercase(Locale.getDefault()).replace(" ", "_")
                + "_" + name.lowercase(Locale.getDefault()).replace(" ", "_"),
        description
    )

    override val defaultValue = valueIn
    override var value = valueIn
        set(value) {
            if (value != field) {
                val prev = field
                val new = value
                field = new
                SpartanCore.updateSettings()
                valueListeners.forEach { it(prev, field) }
                listeners.forEach { it() }
            }
        }

    fun des(language: InnerLanguage, description: String): MutableSetting<T> {
        this.description.add(language, description)
        return this
    }

    fun des(): MutableSetting<T> {
        return this
    }

    infix fun ch(description: String): MutableSetting<T> {
        return des(InnerLanguage.Chinese, description)
    }

    infix fun jp(description: String): MutableSetting<T> {
        return des(InnerLanguage.Japanese, description)
    }

    infix fun ru(description: String): MutableSetting<T> {
        return des(InnerLanguage.Russian, description)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

}