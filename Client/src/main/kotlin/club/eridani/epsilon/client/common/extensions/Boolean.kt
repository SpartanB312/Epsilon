package club.eridani.epsilon.client.common.extensions

import club.eridani.epsilon.client.setting.AbstractSetting
import club.eridani.epsilon.client.setting.impl.primitive.EnumSetting

fun <E : Enum<E>> Enum<E>.notAtValue(value: E): () -> Boolean = {
    this != value
}

fun <T : Any> AbstractSetting<T>.notAtValue(value: T): () -> Boolean = {
    this.value != value
}

fun <E : Enum<E>> atMode(enumSetting: EnumSetting<E>, mode: E): () -> Boolean = {
    enumSetting.value == mode
}

fun <T : Any> AbstractSetting<T>.atValue(value: T): () -> Boolean = {
    this.value == value
}

fun <T : Any> AbstractSetting<T>.atValue(value1: T, value2: T): () -> Boolean = {
    this.value == value1 || this.value == value2
}

fun AbstractSetting<Boolean>.atTrue(): () -> Boolean {
    return {
        this.value
    }
}

fun AbstractSetting<Boolean>.atFalse(): () -> Boolean = {
    !this.value
}

inline infix fun (() -> Boolean).or(crossinline block: () -> Boolean): () -> Boolean = {
    this.invoke() || block.invoke()
}

inline infix fun (() -> Boolean).and(crossinline block: () -> Boolean): () -> Boolean = {
    this.invoke() && block.invoke()
}