package club.eridani.epsilon.client.gui

import club.eridani.epsilon.client.setting.AbstractSetting

interface ISettingSupplier<T> {
    val setting: AbstractSetting<T>
}