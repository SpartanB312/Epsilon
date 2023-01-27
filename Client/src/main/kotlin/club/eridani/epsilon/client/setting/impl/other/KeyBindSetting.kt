package club.eridani.epsilon.client.setting.impl.other

import club.eridani.epsilon.client.common.extensions.copy
import club.eridani.epsilon.client.common.key.KeyBind
import club.eridani.epsilon.client.management.InputManager.register
import club.eridani.epsilon.client.setting.MutableSetting

class KeyBindSetting(
    name: String,
    value: KeyBind,
    visibility: (() -> Boolean) = { true },
    moduleName: String,
    description: String = ""
) : MutableSetting<KeyBind>(name, value, moduleName, description, visibility) {

    private val defKeyCodes = value.key.copy()

    override fun reset() {
        this.value.key = defKeyCodes
    }

    init {
        this.value.register()
    }

}