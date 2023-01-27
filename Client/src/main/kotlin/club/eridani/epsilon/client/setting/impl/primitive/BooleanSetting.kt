package club.eridani.epsilon.client.setting.impl.primitive

import club.eridani.epsilon.client.setting.MutableSetting

open class BooleanSetting(
    name: String,
    value: Boolean,
    visibility: (() -> Boolean) = { true },
    moduleName: String,
    description: String = ""
) : MutableSetting<Boolean>(name, value, moduleName, description, visibility)