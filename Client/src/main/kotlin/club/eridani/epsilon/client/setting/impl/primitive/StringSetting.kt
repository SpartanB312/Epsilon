package club.eridani.epsilon.client.setting.impl.primitive

import club.eridani.epsilon.client.setting.MutableSetting

class StringSetting(
    name: String,
    value: String,
    visibility: (() -> Boolean) = { true },
    moduleName: String,
    description: String = ""
) : MutableSetting<String>(name, value, moduleName, description, visibility)