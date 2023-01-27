package club.eridani.epsilon.client.module.client

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.config.ConfigManager
import club.eridani.epsilon.client.gui.def.DefaultRootScreen
import club.eridani.epsilon.client.menu.main.MainMenu
import club.eridani.epsilon.client.module.Module
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard

internal object RootGUI : Module(
    name = "RootGUI",
    visibleOnArray = false,
    alias = arrayOf("Gui", "ClickGUI", "MainGUI"),
    category = Category.Client,
    description = "The root GUI of Epsilon",
    keyBind = Keyboard.KEY_RSHIFT
) {

    private var lastGuiScreen: GuiScreen? = null
    private val zoomIn by setting("ZoomIn ", true)

    private var cancelZoomIn = false

    override fun onEnable() {
        if (Epsilon.isReady) {
            val screen = getStyledScreen()
            if (mc.currentScreen != screen) {
                mc.displayGuiScreen(screen)
                lastGuiScreen = screen
            }
            if (cancelZoomIn) {
                cancelZoomIn = false
            } else {
                val zoomAnimationTime = if (zoomIn) System.currentTimeMillis() else 0L
                DefaultRootScreen.openTime = zoomAnimationTime
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
            GuiSetting.Style.Default -> DefaultRootScreen
        }
         */
        return DefaultRootScreen
    }

}