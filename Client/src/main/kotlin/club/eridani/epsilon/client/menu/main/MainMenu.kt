package club.eridani.epsilon.client.menu.main

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.gui.SpartanScreen
import club.eridani.epsilon.client.management.Fonts
import club.eridani.epsilon.client.menu.alt.AltManagerGui
import club.eridani.epsilon.client.menu.main.elements.MainSelectButton
import club.eridani.epsilon.client.module.client.RootGUI
import club.eridani.epsilon.client.module.setting.MenuSetting
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.graphics.ResolutionHelper
import net.minecraft.client.gui.GuiLanguage
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiWorldSelection
import net.minecraft.util.math.MathHelper
import net.minecraftforge.fml.client.GuiModList
import org.lwjgl.input.Keyboard

object MainMenu : SpartanScreen() {

    private val buttons = mutableListOf<MainSelectButton>()
    private val topButtons = mutableListOf<MainSelectButton>()

    init {
        buttons.add(MainSelectButton("SinglePlayer", actionButton(Action.SinglePlayer)))
        buttons.add(MainSelectButton("MultiPlayer", actionButton(Action.MultiPlayer)))
        buttons.add(MainSelectButton("Language", actionButton(Action.Language)))
        buttons.add(MainSelectButton("Options", actionButton(Action.Options)))
        buttons.add(MainSelectButton("Mods", actionButton(Action.Mods)))

        topButtons.add(MainSelectButton("X", actionButton(Action.Exit)))
        topButtons.add(MainSelectButton("AltManager", actionButton(Action.AltManager)))
        topButtons.add(MainSelectButton("Config", actionButton(Action.Config)))
    }

    var notReset = false
    var lastUpdateTime = 0L
    var currentControllerType = ControllerType.Mouse
    var currentElement: MainSelectButton = buttons.first()
    private var categoryPosition = 0

    override fun initGui() {
        if (notReset) {
            notReset = false
        } else MenuSetting.reset()
    }

    fun onDirection(direction: Direction) {
        when (direction) {
            Direction.Up -> {
                if (categoryPosition == 0) {
                    val index = buttons.indexOf(currentElement)
                    currentElement = if (index == 0) buttons.last() else buttons[index - 1]
                }
            }
            Direction.Down -> {
                if (categoryPosition == 0) {
                    val index = buttons.indexOf(currentElement)
                    currentElement = if (index == buttons.size - 1) buttons.first() else buttons[index + 1]
                }
            }
            Direction.Left -> {
                if (categoryPosition == 1) {
                    val index = topButtons.indexOf(currentElement)
                    if (index < topButtons.size - 1) {
                        currentElement = topButtons[index + 1]
                    } else if (index == topButtons.size - 1) {
                        categoryPosition = 0
                        currentElement = buttons.first()
                    }
                }
            }
            Direction.Right -> {
                if (categoryPosition == 1) {
                    val index = topButtons.indexOf(currentElement)
                    if (index > 0) currentElement = topButtons[index - 1]
                } else if (categoryPosition == 0) {
                    categoryPosition = 1
                    currentElement = topButtons.last()
                }
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        MenuSetting.drawBackground(mouseX, mouseY, this)

        if (currentControllerType != ControllerType.Mouse && System.currentTimeMillis() - lastUpdateTime >= 3000)
            currentControllerType = ControllerType.Mouse

        val width = ResolutionHelper.width
        val height = ResolutionHelper.height

        val startX = width / 16.0f
        var startY = height * 0.45f

        val size = 0.3f * (height / 540f)
        //Logo
        Fonts.logoFont.drawString(
            Epsilon.MOD_NAME,
            startX,
            height * 0.25f,
            ColorRGB(255, 255, 255, 255),
            size * 1.75f,
        )

        Fonts.smallFont.drawString(
            Epsilon.INFO,
            1f,
            height - 1 - Fonts.smallFont.getHeight() * size * 0.5f,
            ColorRGB(220, 220, 220, 155),
            size * 0.5f
        )

        //Buttons
        buttons.forEach {

            val isSelected = it.getSelected(mouseX, mouseY)

            if (isSelected) {
                if (it.size < 1.3f) {
                    it.size += 0.06f * it.size
                }
            }

            it.x = (startX + size * 14).toInt()
            it.y = startY.toInt()
            it.width = (Fonts.smallFont.getWidth(it.name) * it.size * size).toInt()
            it.height = (Fonts.smallFont.getHeight() * it.size * size).toInt()

            Fonts.smallFont.drawString(
                it.name,
                it.x.toFloat(),
                it.y.toFloat(),
                ColorRGB(
                    220, 220, 220, MathHelper.clamp(
                        155 + (100 * (it.size - 1.0f) / 0.3f).toInt(),
                        0, 255
                    )
                ),
                it.size * size,
            )

            if (it.size > 1.0f && !isSelected) {
                it.size -= 0.03f * it.size
            }

            startY += (it.height + 1)
        }

        drawTop(mouseX, mouseY, width.toFloat(), height.toFloat())
    }

    private fun MainSelectButton.getSelected(mouseX: Int, mouseY: Int): Boolean {
        val isController = club.eridani.epsilon.client.menu.main.MainMenu.currentControllerType != ControllerType.Mouse && currentElement == this
        val isMouse = club.eridani.epsilon.client.menu.main.MainMenu.currentControllerType == ControllerType.Mouse
        val isHoovered = this.isHoovered(mouseX, mouseY)
        if (isHoovered) {
            club.eridani.epsilon.client.menu.main.MainMenu.categoryPosition = if (buttons.indexOf(this) != -1) 0
            else 1
            currentElement = this
            if (!isMouse) club.eridani.epsilon.client.menu.main.MainMenu.currentControllerType = ControllerType.Mouse
        }
        return isController || (isMouse && isHoovered)
    }

    private fun drawTop(mouseX: Int, mouseY: Int, width: Float, height: Float) {
        val startY = height * 0.02f
        var startX = width * 0.985f

        val gap = width * 0.03f
        val size = 0.25f * (height / 540f)

        val fontHeight = (Fonts.icon.getHeight() * size).toInt()

        topButtons.forEach {
            it.alpha = AnimationUtil.animate(
                if (it.getSelected(mouseX, mouseY)) 100.0 else 0.0, it.alpha.toDouble(), 0.2
            ).toInt()
            when (it.name) {
                "X" -> {
                    it.y = (startY - fontHeight * 0.10f).toInt()
                    val tempWidth: Float = Fonts.icon.getWidth("X") * size * 1.5f
                    Fonts.icon.drawString(
                        "X",
                        startX - tempWidth,
                        it.y.toFloat(),
                        scale = size * 1.5f,
                        color = ColorRGB(220, 220, 220, 155 + it.alpha)
                    )
                    it.width = tempWidth.toInt()
                }
                else -> {
                    it.y = startY.toInt()
                    val stringWidth = Fonts.smallFont.getWidth(it.name) * size
                    Fonts.smallFont.drawString(
                        it.name,
                        startX - stringWidth,
                        startY,
                        scale = size,
                        color = ColorRGB(220, 220, 220, 155 + it.alpha)
                    )
                    val iconWidth: Float = Fonts.icon.getWidth(getIcon(it.name)) * size
                    Fonts.icon.drawString(
                        getIcon(it.name),
                        startX - stringWidth - iconWidth - gap * 0.25f,
                        startY,
                        ColorRGB(255, 255, 255),
                        scale = size
                    )
                    it.width = (iconWidth + stringWidth + gap * 0.25f).toInt()
                }
            }
            it.height = fontHeight
            it.x = (startX - it.width).toInt()
            startX = it.x - gap
        }
    }

    private fun getIcon(string: String): String = when (string) {
        "AltManager" -> "c"
        else -> "d"
    }

    var lastKeyTime = 0L

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (System.currentTimeMillis() - lastKeyTime >= 100) {
            when (keyCode) {
                Keyboard.KEY_UP -> {
                    onDirection(Direction.Up)
                    currentControllerType = ControllerType.Keyboard
                    lastUpdateTime = System.currentTimeMillis()
                }
                Keyboard.KEY_DOWN -> {
                    onDirection(Direction.Down)
                    currentControllerType = ControllerType.Keyboard
                    lastUpdateTime = System.currentTimeMillis()
                }
                Keyboard.KEY_LEFT -> {
                    onDirection(Direction.Left)
                    currentControllerType = ControllerType.Keyboard
                    lastUpdateTime = System.currentTimeMillis()
                }
                Keyboard.KEY_RIGHT -> {
                    onDirection(Direction.Right)
                    currentControllerType = ControllerType.Keyboard
                    lastUpdateTime = System.currentTimeMillis()
                }
                Keyboard.KEY_RETURN -> {
                    currentElement.action.invoke()
                    currentControllerType = ControllerType.Keyboard
                    lastUpdateTime = System.currentTimeMillis()
                }
            }
            lastKeyTime = System.currentTimeMillis()
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        topButtons.forEach {
            if (it.isHoovered(mouseX, mouseY) && mouseButton == 0) {
                it.action.invoke()
                return
            }
        }
        buttons.forEach {
            if (it.isHoovered(mouseX, mouseY) && mouseButton == 0) {
                it.action.invoke()
                return
            }
        }
    }

    private enum class Action {
        SinglePlayer,
        MultiPlayer,
        Language,
        Options,
        Config,
        Mods,
        AltManager,
        Exit
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun actionButton(action: Action): () -> Unit = {
        when (action) {
            Action.SinglePlayer -> mc.displayGuiScreen(GuiWorldSelection(this))
            Action.MultiPlayer -> mc.displayGuiScreen(GuiMultiplayer(this))
            Action.Language -> mc.displayGuiScreen(GuiLanguage(this, mc.gameSettings, mc.languageManager))
            Action.Options -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            Action.Config -> RootGUI.enable(notification = false)
            Action.Mods -> mc.displayGuiScreen(GuiModList(this))
            Action.AltManager -> mc.displayGuiScreen(AltManagerGui(this))
            Action.Exit -> mc.shutdown()
        }
    }

}
