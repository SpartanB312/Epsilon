package club.eridani.epsilon.client.gui.def

import club.eridani.epsilon.client.gui.IComponent
import club.eridani.epsilon.client.gui.def.components.elements.other.SBBox
import club.eridani.epsilon.client.gui.def.theme.IDefaultBothTheme
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.SpartanCore.addAsyncUpdateListener
import club.eridani.epsilon.client.module.setting.GuiSetting
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.graphics.font.renderer.IFontRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.RenderTask
import club.eridani.epsilon.client.util.graphics.render.render.CircleRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.LineRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.RectRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.TextRenderTask
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
object AsyncRenderEngine {

    private val tasks = mutableListOf<RenderTask>()
    val tempTasks = mutableListOf<RenderTask>()
    lateinit var currentTheme: IDefaultBothTheme

    var currentSBBOX: SBBox? = null
    var noClear = false

    fun clearSBBox() {
        if (noClear) {
            noClear = false
        } else {
            currentSBBOX = null
        }
    }

    fun init() {
        addAsyncUpdateListener {
            if (GuiSetting.asyncGUI) {
                val scaledResolution = ScaledResolution(Wrapper.mc)
                val i1 = scaledResolution.scaledWidth
                val j1 = scaledResolution.scaledHeight
                val mouseX: Int = Mouse.getX() * i1 / Wrapper.mc.displayWidth
                val mouseY: Int = j1 - Mouse.getY() * j1 / Wrapper.mc.displayHeight - 1
                update(mouseX, mouseY, Wrapper.mc.renderPartialTicks)
            }
        }
    }

    fun update(mouseX: Int, mouseY: Int, partialTicks: Float) {
        currentTheme = ThemeContainer.syncTheme()!!
        tempTasks.clear()
        if (Wrapper.mc.currentScreen == DefaultHUDEditorScreen) {
            DefaultHUDEditorScreen.onUpdate(mouseX, mouseY, partialTicks)
        } else if (Wrapper.mc.currentScreen == DefaultRootScreen) {
            DefaultRootScreen.onUpdate(mouseX, mouseY, partialTicks)
        }
        currentSBBOX?.onRender(mouseX, mouseY, partialTicks)
        GUIManager.defaultGUI.drawDescription(this, mouseX, mouseY)
        synchronized(tasks) {
            tasks.clear()
            tasks.addAll(tempTasks)
        }
    }

    val render: () -> Unit = {
        val copied: List<RenderTask>
        synchronized(tasks) {
            copied = tasks.toList()
        }
        copied.forEach {
            it.onRender()
        }
    }

    inline fun IComponent.draw(
        crossinline task: () -> Unit
    ) {
        tempTasks.add(object : RenderTask {
            override fun onRender() {
                task.invoke()
            }
        })
    }

    fun inArea(mouseX: Int, mouseY: Int, x: Int, y: Int, endX: Int, endY: Int): Boolean {
        return mouseX >= min(x, endX) && mouseX <= max(x, endX)
                && mouseY >= min(y, endY) && mouseY <= max(y, endY)
    }

    inline fun AsyncRenderEngine.draw(
        crossinline task: () -> Unit
    ) {
        tempTasks.add(object : RenderTask {
            override fun onRender() {
                task.invoke()
            }
        })
    }

    inline fun IComponent.drawRect(
        x: Float,
        y: Float,
        endX: Float,
        endY: Float,
        color1: ColorRGB,
        color2: ColorRGB = color1,
        color3: ColorRGB = color1,
        color4: ColorRGB = color1,
        filled: Boolean = true
    ) {
        tempTasks.add(RectRenderTask(x, y, endX, endY, color1, color2, color3, color4, filled))
    }

    inline fun IComponent.drawString(
        text: String,
        x: Float,
        y: Float,
        colorRGB: ColorRGB,
        scale: Float = 1.0F,
        font: IFontRenderer = MainFontRenderer
    ) {
        tempTasks.add(TextRenderTask(text, x, y, colorRGB, scale, false, font))
    }

    inline fun IComponent.drawStringWithShadow(
        text: String,
        x: Float,
        y: Float,
        colorRGB: ColorRGB,
        scale: Float = 1.0F,
        font: IFontRenderer = MainFontRenderer
    ) {
        tempTasks.add(TextRenderTask(text, x, y, colorRGB, scale, true, font))
    }

    inline fun IComponent.drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: ColorRGB,
        color2: ColorRGB = color,
    ) {
        tempTasks.add(LineRenderTask(startX, startY, endX, endY, color, color2))
    }

    inline fun IComponent.drawCircle(
        x: Float,
        y: Float,
        r: Float,
        color: ColorRGB,
        filled: Boolean = true
    ) {
        tempTasks.add(CircleRenderTask(x, y, r, color, filled))
    }

    inline fun drawRect(
        x: Float,
        y: Float,
        endX: Float,
        endY: Float,
        color1: ColorRGB,
        color2: ColorRGB = color1,
        color3: ColorRGB = color1,
        color4: ColorRGB = color1,
        filled: Boolean = true
    ) {
        tempTasks.add(RectRenderTask(x, y, endX, endY, color1, color2, color3, color4, filled))
    }

    inline fun drawString(
        text: String,
        x: Float,
        y: Float,
        colorRGB: ColorRGB,
        scale: Float = 1.0F,
        font: IFontRenderer = MainFontRenderer
    ) {
        tempTasks.add(TextRenderTask(text, x, y, colorRGB, scale, false, font))
    }

    inline fun drawStringWithShadow(
        text: String,
        x: Float,
        y: Float,
        colorRGB: ColorRGB,
        scale: Float = 1.0F,
        font: IFontRenderer = MainFontRenderer
    ) {
        tempTasks.add(TextRenderTask(text, x, y, colorRGB, scale, true, font))
    }

    inline fun drawLine(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        color: ColorRGB,
        color2: ColorRGB = color,
    ) {
        tempTasks.add(LineRenderTask(startX, startY, endX, endY, color, color2))
    }

    inline fun drawCircle(
        x: Float,
        y: Float,
        r: Float,
        color: ColorRGB,
        filled: Boolean = true
    ) {
        tempTasks.add(CircleRenderTask(x, y, r, color, filled))
    }
}