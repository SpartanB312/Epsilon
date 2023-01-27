package club.eridani.epsilon.client.gui.def.components

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.gui.Dragging
import club.eridani.epsilon.client.gui.IChildComponent
import club.eridani.epsilon.client.gui.IFatherExtendable
import club.eridani.epsilon.client.gui.def.AsyncRenderEngine
import club.eridani.epsilon.client.gui.def.DefaultHUDEditorScreen
import club.eridani.epsilon.client.gui.def.DefaultRootScreen
import club.eridani.epsilon.client.gui.def.components.elements.ModuleButton
import club.eridani.epsilon.client.management.ModuleManager
import club.eridani.epsilon.client.management.SpartanCore
import club.eridani.epsilon.client.module.setting.FontSetting
import club.eridani.epsilon.client.module.setting.GuiSetting
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import net.minecraft.util.math.MathHelper
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Panel(
    val category: Category,
    override var x: Int = 0,
    override var y: Int = 0,
    override var width: Int,
    override var height: Int,
    private val isHUD: Boolean = false
) : IFatherExtendable {

    override var isActive: Boolean = true
    override var children: MutableList<IChildComponent> = mutableListOf()
    override var visibleChildren = listOf<IChildComponent>()

    private var trX = 0
    private var trY = 0

    fun translate(x: Int, y: Int) {
        trX = x
        trY = y
    }

    val translatedX get() = trX + x
    val translatedY get() = trY + y

    private val adjustTimer = TickTimer()

    fun adjust() {
        if (GuiSetting.autoAdjust && adjustTimer.passed(500)) {
            adjustTimer.reset()
            val length = lastEndY - extendableStartY
            if (length > limit) {
                if (endY < downLimit) {
                    targetOffsetY = limit - length
                }
            } else targetOffsetY = 0
        }
    }

    var targetOffsetY = 0
        set(value) {
            field = if (value > height) height
            else value
        }

    var offsetY = 0
        set(value) {
            field = MathHelper.clamp(value, -(endY - extendableStartY), 0)
        }

    var lastEndY = 0
    val endY get() = min(lastEndY, downLimit)

    inline val limit get() = if (GuiSetting.unlimitedLength) 114514 else GuiSetting.maxLength

    inline val upLimit get() = translatedY + height
    inline val downLimit get() = translatedY + height + limit

    inline val extendableStartY get() = translatedY + offsetY + height

    init {
        SpartanCore.registerExtendable(this)
    }

    private var offsetUpdateTimer = TickTimer()

    override fun onRender(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (offsetUpdateTimer.passed(17)) {
            offsetUpdateTimer.reset()
            offsetY = AnimationUtil.animate(targetOffsetY.toDouble(), offsetY.toDouble(), 0.1).toInt()
        }
        dragging.updatePos(mouseX, mouseY)
        this.height = Scale.panelHeight
        this.width = (45 + 55 * FontSetting.sizeSetting.value).toInt()
        AsyncRenderEngine.currentTheme.panel(this, mouseX, mouseY, partialTicks)
    }

    override var current = 0
    override var target = 0
    override var isPaused = false
    override val timer = Timer()

    private val dragging = Dragging(this)

    init {
        ModuleManager.modules.forEach {
            if (it.category == category)
                children.add(ModuleButton(it, this, panel = this))
        }
    }

    override fun onMouseClicked(x: Int, y: Int, button: Int): Boolean {
        if (button == 0 && isHoovered(x, y)) {
            dragging.onClick(x, y, button)
            if (isHUD)
                Collections.swap(DefaultHUDEditorScreen.panels, 0, DefaultHUDEditorScreen.panels.indexOf(this))
            else
                Collections.swap(DefaultRootScreen.panels, 0, DefaultRootScreen.panels.indexOf(this))
            return true
        }
        if (button == 1 && isHoovered(x, y)) {
            isActive = !isActive
            if (!isActive) {
                children.forEach {
                    (it as IFatherExtendable).isActive = false
                }
            }
            return true
        }
        return false
    }

    override fun onMouseReleased(x: Int, y: Int, state: Int) {
        dragging.release(state)
        for (part in children) {
            part.onMouseReleased(x, y, state)
        }
    }

    override fun keyTyped(char: Char, key: Int): Boolean {
        for (part in children) {
            if (part.keyTyped(char, key)) return true
        }
        return false
    }

    fun hooveredInDrawnPanel(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= min(translatedX, translatedX + width) && mouseX <= max(translatedX, translatedX + width)
                && mouseY >= min(translatedY, endY) && mouseY <= max(translatedY, endY)
    }

    override fun isHoovered(mouseX: Int, mouseY: Int, predicate: Boolean): Boolean {
        return mouseX >= min(translatedX, translatedX + width) && mouseX <= max(translatedX, translatedX + width)
                && mouseY >= min(translatedY, translatedY + height) && mouseY <= max(translatedY, translatedY + height)
                && predicate
    }

}