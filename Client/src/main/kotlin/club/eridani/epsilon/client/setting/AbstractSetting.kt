package club.eridani.epsilon.client.setting

import club.eridani.epsilon.client.common.interfaces.Nameable
import club.eridani.epsilon.client.language.TextUnit
import kotlin.reflect.KProperty

@Suppress("NOTHING_TO_INLINE")
abstract class AbstractSetting<T> : Nameable {

    abstract override val name: String
    abstract val defaultValue: T
    abstract val description: TextUnit
    abstract val visibility: (() -> Boolean)

    abstract var value: T

    var isVisible = true
    val isModified get() = this.value != this.defaultValue

    val listeners = ArrayList<() -> Unit>()
    val valueListeners = ArrayList<(prev: T, input: T) -> Unit>()

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    open fun reset() {
        value = defaultValue
    }

    infix fun listen(listener: () -> Unit): AbstractSetting<T> {
        this.listeners.add(listener)
        return this
    }

    fun valueListen(listener: (prev: T, input: T) -> Unit) {
        this.valueListeners.add(listener)
    }

    override fun toString() = value.toString()

    override fun equals(other: Any?) = this === other
            || (other is AbstractSetting<*>
            && this.name == other.name
            && this.value == other.value)

    override fun hashCode() = name.hashCode() * 31 + value.hashCode()

}