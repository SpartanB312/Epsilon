package club.eridani.epsilon.client.gui.def

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.gui.IFloatAnimatable
import club.eridani.epsilon.client.gui.SpartanScreen
import club.eridani.epsilon.client.gui.def.components.IAnimatable
import club.eridani.epsilon.client.gui.def.components.Panel
import club.eridani.epsilon.client.gui.def.components.ParticleRenderer
import club.eridani.epsilon.client.gui.def.components.elements.ModuleButton
import club.eridani.epsilon.client.gui.def.components.elements.other.HUDFrame
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.client.HUDEditor
import club.eridani.epsilon.client.module.setting.GuiSetting
import club.eridani.epsilon.client.module.setting.MenuSetting
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.shaders.WindowBlurShader
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.*

object DefaultHUDEditorScreen : SpartanScreen() {

    val panels = mutableListOf<Panel>()
    val hudList = mutableListOf<HUDFrame>()

    init {
        var startX = 5
        Category.values().forEach {
            if (it != Category.Hidden) {
                if (it.isHUD) {
                    panels.add(Panel(it, startX, 5, 100, 15, true))
                    startX += 105
                }
            }
        }
        panels.forEach { panel ->
            panel.children.forEach {
                hudList.add(((it as ModuleButton).module as HUDModule).hudFrame)
            }
        }
    }

    override fun onUpdate(mouseX: Int, mouseY: Int, partialTicks: Float) {
        panels.reversed().forEach {
            it.onRender(mouseX, mouseY, partialTicks)
        }
    }


    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (mc.player == null) MenuSetting.drawBackground(mouseX, mouseY, this)
        if (GUIManager.isBlur) WindowBlurShader.render(width.toFloat(), height.toFloat())
        if (GUIManager.isShadow) {
            RenderUtils2D.drawGradientRect(
                0f, 0f, width.toFloat(), height.toFloat(),
                GUIManager.firstColor.alpha(0),
                GUIManager.firstColor.alpha(0),
                GUIManager.firstColor.alpha(128),
                GUIManager.firstColor.alpha(128)
            )
        }
//        if (mc.player == null) drawRect(0, 0, 9999, 9999, Color(0, 0, 0, 255).rgb)
        if (GUIManager.isParticle) {
            ParticleRenderer.tick(10)
            ParticleRenderer.render()
        }

        //HUDs
        hudList.reversed().forEach {
            it.onRender(mouseX, mouseY, partialTicks)
        }

        //Panels
        mouseDrag(mouseX, mouseY)
        if (!GuiSetting.asyncGUI) AsyncRenderEngine.update(mouseX, mouseY, partialTicks)
        AsyncRenderEngine.render()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        AsyncRenderEngine.currentSBBOX?.let {
            if (it.onMouseClicked(mouseX, mouseY, mouseButton)) return
        }
        for (panel in panels) {
            if (panel.onMouseClicked(mouseX, mouseY, mouseButton)) {
                AsyncRenderEngine.clearSBBox()
                return
            }
            if (!panel.isActive) continue
            for (part in panel.children) {
                if (part.onMouseClicked(mouseX, mouseY, mouseButton)) {
                    AsyncRenderEngine.clearSBBox()
                    return
                }
                if (!(part as ModuleButton).isActive) continue
                for (component in part.children) {
                    if (!component.isVisible()) continue
                    if (component.onMouseClicked(mouseX, mouseY, mouseButton)) {
                        AsyncRenderEngine.clearSBBox()
                        return
                    }
                }
            }
        }
        for (hud in hudList) {
            if (hud.onMouseClicked(mouseX, mouseY, mouseButton)) {
                Collections.swap(hudList, 0, hudList.indexOf(hud))
                AsyncRenderEngine.clearSBBox()
                return
            }
        }
        AsyncRenderEngine.clearSBBox()
    }

    override fun onGuiClosed() {
        //reset animation
        panels.forEach { panel ->
            panel.children.forEach {
                when (it) {
                    is ModuleButton -> {
                        for (component in it.children) {
                            if (component is IFloatAnimatable) {
                                component.currentValue = 0F
                            }
                            if (component is IAnimatable) {
                                component.animatedAlphaUnit.currentValue = 0F
                            }
                        }
                    }
                }
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            HUDEditor.disable(notification = false)
            return
        }
        panels.forEach {
            if (it.keyTyped(typedChar, keyCode)) return
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        panels.forEach {
            it.onMouseReleased(mouseX, mouseY, state)
        }
        hudList.forEach {
            it.onMouseReleased(mouseX, mouseY, state)
        }
    }

    private fun mouseDrag(mouseX: Int, mouseY: Int) {
        val dWheel = Mouse.getDWheel()
        if (dWheel != 0) {
            val hooveredPanel = panels.firstOrNull { it.hooveredInDrawnPanel(mouseX, mouseY) }
            if (hooveredPanel != null) {
                hooveredPanel.targetOffsetY = hooveredPanel.targetOffsetY + dWheel / 3
            } else {
                if (dWheel < 0) {
                    panels.forEach { component: Panel -> component.y -= 10 }
                } else if (dWheel > 0) {
                    panels.forEach { component: Panel -> component.y += 10 }
                }
            }
        }
    }

}