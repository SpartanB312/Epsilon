package club.eridani.epsilon.client.common

import club.eridani.epsilon.client.common.extensions.notAtValue
import club.eridani.epsilon.client.common.interfaces.Alias
import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.common.interfaces.Nameable
import club.eridani.epsilon.client.common.key.KeyBind
import club.eridani.epsilon.client.config.ConfigManager
import club.eridani.epsilon.client.config.ModuleConfig
import club.eridani.epsilon.client.event.decentralized.IDecentralizedEvent
import club.eridani.epsilon.client.event.decentralized.Listenable
import club.eridani.epsilon.client.language.InnerLanguage
import club.eridani.epsilon.client.language.TextUnit
import club.eridani.epsilon.client.management.SpartanCore
import club.eridani.epsilon.client.module.client.HUDEditor
import club.eridani.epsilon.client.module.client.RootGUI
import club.eridani.epsilon.client.notification.Notification
import club.eridani.epsilon.client.notification.NotificationManager
import club.eridani.epsilon.client.notification.NotificationType
import club.eridani.epsilon.client.setting.AbstractSetting
import club.eridani.epsilon.client.setting.SettingRegister
import club.eridani.epsilon.client.util.IDRegistry
import java.util.*

@Suppress("LeakingThis")
abstract class AbstractModule(
    override var name: String,
    override val alias: Array<String> = emptyArray(),
    val category: Category,
    description: String,
    val priority: Int,
    keyCode: Int,
    visibility: Boolean,
) : Nameable, Alias, Listenable, SettingRegister<AbstractModule>, Helper, Comparable<AbstractModule> {

    override val subscribedListener = ArrayList<Triple<IDecentralizedEvent<*>, (Any) -> Unit, Int>>()

    var currentConfig = "Default"

    val description = TextUnit("module_" + name.lowercase(Locale.getDefault()).replace(" ", "_"), description)
    val keyBind: KeyBind = KeyBind(keyCode, action = { toggle() })
    var isEnabled = false
    val isDisabled get() = !isEnabled
    var toggleTime = System.currentTimeMillis()
    private val keyListeners = mutableListOf<(Int) -> Unit>()

    val id = idRegistry.register()

    override fun compareTo(other: AbstractModule): Int {
        val result = this.priority.compareTo(other.priority)
        if (result != 0) return result
        return this.id.compareTo(other.id)
    }

    open fun isActive(): Boolean {
        return isEnabled
    }

    val config = ModuleConfig(this).also {
        ConfigManager.register(it)
    }

    enum class Visibility {
        ON, OFF
    }

    val reset: () -> Unit = {
        if (category != Category.Setting && this != RootGUI && this != HUDEditor) disable(notification = false)
        config.configs.forEach {
            it.reset()
        }
    }

    val visibilitySetting by setting(
        "Visibility",
        if (!visibility || category == Category.Setting) Visibility.OFF else Visibility.ON,
        "Determine whether the module should be displayed on array",
        category.notAtValue(Category.Setting)
    )

    private val keyBindSetting by setting(
        "Bind",
        keyBind,
        "Bind a key to toggle this module",
        category.notAtValue(Category.Setting)
    )

    private val resetSetting by setting("Reset", reset, "Click here to reset this module")

    fun ch(description: String): AbstractModule {
        return des(InnerLanguage.Chinese, description)
    }

    fun jp(description: String): AbstractModule {
        return des(InnerLanguage.Japanese, description)
    }

    fun ru(description: String): AbstractModule {
        return des(InnerLanguage.Russian, description)
    }

    private fun des(language: InnerLanguage, description: String): AbstractModule {
        this.description.add(language, description)
        return this
    }

    fun saveConfig() {
        config.saveConfig()
        NotificationManager.show(
            Notification(
                message = "Saved config for module ${this.name}",
                type = NotificationType.DEBUG
            )
        )
    }

    fun loadConfig() {
        config.loadConfig()
        NotificationManager.show(
            Notification(
                message = "Loaded config for module ${this.name}",
                type = NotificationType.DEBUG
            )
        )
    }

    open fun onEnable() {
    }

    open fun onDisable() {
    }

    fun enable(notification: Boolean = true, silent: Boolean = false) {
        toggleTime = System.currentTimeMillis()
        if (category == Category.Setting) {
            if (notification) NotificationManager.show(
                Notification(
                    message = "You aren't allowed to enable an always disabled setting modules.",
                    type = NotificationType.WARNING
                )
            )
            return
        }
        if (notification) NotificationManager.show(
            Notification(
                module = this,
                message = this.name + " is Enabled",
                type = NotificationType.MODULE
            )
        )
        isEnabled = true
        SpartanCore.register(this)
        if (!silent) onEnable()
    }

    fun disable(notification: Boolean = true, silent: Boolean = false) {
        toggleTime = System.currentTimeMillis()
        if (notification) NotificationManager.show(
            Notification(
                module = this,
                message = this.name + " is Disabled",
                type = NotificationType.MODULE
            )
        )

        isEnabled = false
        SpartanCore.unregister(this)
        if (!silent) onDisable()
    }

    abstract fun onAsyncUpdate(block: () -> Unit)

    fun toggle() {
        if (isEnabled) disable()
        else enable()
    }

    open fun getHudInfo(): String? {
        return null
    }

    override fun <S : AbstractSetting<*>> AbstractModule.setting(setting: S): S {
        SpartanCore.registerSetting(setting)
        this.config.configs.add(setting)
        return setting
    }

    protected companion object {
        val idRegistry = IDRegistry()
    }

}