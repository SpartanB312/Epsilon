package club.eridani.epsilon.client.module.client

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.config.ConfigManager
import club.eridani.epsilon.client.gui.def.DefaultHUDEditorScreen
import club.eridani.epsilon.client.menu.main.MainMenu
import club.eridani.epsilon.client.module.Module
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard

internal object HUDEditor : Module(
    name = "HUDEditor",
    category = Category.Client,
    visibleOnArray = false,
    description = "Edit your HUD",
    keyBind = Keyboard.KEY_GRAVE
) {

    private var lastGuiScreen: GuiScreen? = null

    fun isHUDEditor(): Boolean {
        return when (mc.currentScreen) {
            null -> false
            lastGuiScreen, getStyledScreen() -> true
            else -> false
        }
    }

    override fun onEnable() {
        if (Epsilon.isReady) {
            val screen = getStyledScreen()
            if (mc.currentScreen != screen) {
                mc.displayGuiScreen(screen)
                lastGuiScreen = screen
            }
        }
    }

    override fun onDisable() {
        if (mc.currentScreen != null && mc.currentScreen == lastGuiScreen) {
            if (mc.player == null) {
                MainMenu.notReset = true
                mc.displayGuiScreen(MainMenu)
            } else mc.displayGuiScreen(null)
        }
        lastGuiScreen = null
        ConfigManager.saveAll(true)
    }

    private fun getStyledScreen(): GuiScreen {
        /*
        return when (style.value) {
            GuiSetting.Style.Default -> DefaultHUDEditorScreen
        }
         */
        return DefaultHUDEditorScreen
    }

}