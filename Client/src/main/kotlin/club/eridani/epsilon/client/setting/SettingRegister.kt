package club.eridani.epsilon.client.setting

import club.eridani.epsilon.client.common.interfaces.Nameable
import club.eridani.epsilon.client.common.key.KeyBind
import club.eridani.epsilon.client.setting.impl.number.DoubleSetting
import club.eridani.epsilon.client.setting.impl.number.FloatSetting
import club.eridani.epsilon.client.setting.impl.number.IntegerSetting
import club.eridani.epsilon.client.setting.impl.number.LongSetting
import club.eridani.epsilon.client.setting.impl.other.ColorSetting
import club.eridani.epsilon.client.setting.impl.other.KeyBindSetting
import club.eridani.epsilon.client.setting.impl.other.ListenerSetting
import club.eridani.epsilon.client.setting.impl.primitive.BooleanSetting
import club.eridani.epsilon.client.setting.impl.primitive.EnumSetting
import club.eridani.epsilon.client.setting.impl.primitive.StringSetting
import club.eridani.epsilon.client.util.ColorRGB

interface SettingRegister<T : Nameable> {

    fun T.setting(
        name: String,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(DoubleSetting(name, value, range, step, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(FloatSetting(name, value, range, step, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: Int,
        range: IntRange,
        step: Int,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(IntegerSetting(name, value, range, step, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: Long,
        range: LongRange,
        step: Long,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(LongSetting(name, value, range, step, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: ColorRGB,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(ColorSetting(name, value, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: Boolean,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(BooleanSetting(name, value, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: KeyBind,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(KeyBindSetting(name, value, visibility, this.name, description))

    fun <E : Enum<E>> T.setting(
        name: String,
        value: E,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(EnumSetting(name, value, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: String,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(StringSetting(name, value, visibility, this.name, description))

    fun T.setting(
        name: String,
        defaultValue: () -> Unit,
        description: String,
        visibility: () -> Boolean = { true },
    ) = setting(ListenerSetting(name, defaultValue, visibility, this.name, description))

    fun T.setting(
        name: String,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        visibility: () -> Boolean = { true },
    ) = setting(DoubleSetting(name, value, range, step, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        visibility: () -> Boolean = { true },
    ) = setting(FloatSetting(name, value, range, step, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: Int,
        range: IntRange,
        step: Int,
        visibility: () -> Boolean = { true },
    ) = setting(IntegerSetting(name, value, range, step, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: Long,
        range: LongRange,
        step: Long,
        visibility: () -> Boolean = { true },
    ) = setting(LongSetting(name, value, range, step, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: ColorRGB,
        visibility: () -> Boolean = { true },
    ) = setting(ColorSetting(name, value, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: Boolean,
        visibility: () -> Boolean = { true },
    ) = setting(BooleanSetting(name, value, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: KeyBind,
        visibility: () -> Boolean = { true },
    ) = setting(KeyBindSetting(name, value, visibility, this.name, ""))

    fun <E : Enum<E>> T.setting(
        name: String,
        value: E,
        visibility: () -> Boolean = { true },
    ) = setting(EnumSetting(name, value, visibility, this.name, ""))

    fun T.setting(
        name: String,
        value: String,
        visibility: () -> Boolean = { true },
    ) = setting(StringSetting(name, value, visibility, this.name, ""))

    fun T.setting(
        name: String,
        defaultValue: () -> Unit,
        visibility: () -> Boolean = { true },
    ) = setting(ListenerSetting(name, defaultValue, visibility, this.name, ""))

    fun <S : AbstractSetting<*>> T.setting(setting: S): S

}