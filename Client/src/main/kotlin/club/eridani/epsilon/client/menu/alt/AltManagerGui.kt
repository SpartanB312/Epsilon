package club.eridani.epsilon.client.menu.alt

import club.eridani.epsilon.client.management.AltManager
import club.eridani.epsilon.client.management.AltManager.altAccounts
import club.eridani.epsilon.client.management.AltManager.isValidTokenOffline
import club.eridani.epsilon.client.management.AltManager.loadAccount
import club.eridani.epsilon.client.menu.alt.subscreens.AltAddAccount
import club.eridani.epsilon.client.menu.alt.subscreens.AltDirectLogin
import club.eridani.epsilon.client.menu.main.MainMenu
import club.eridani.epsilon.client.mixin.mixins.accessor.AccessorMinecraft
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.drawCenteredString
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSlot
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.*

class AltManagerGui(private val previousScreen: GuiScreen) : GuiScreen() {
    var loginButton: GuiButton? = null
    var altsList: GuiList? = null
    var status: String? = "Idle.."

    private var randomAltButton: GuiButton? = null
    override fun initGui() {
        altsList = GuiList(this)
        altsList!!.registerScrollButtons(7, 8)
        var index = -1
        for (i in altAccounts.indices) {
            val minecraftAccount = altAccounts[i]
            if ((minecraftAccount.passwords == null || minecraftAccount.passwords!!
                    .isEmpty()) && minecraftAccount.username == (mc as AccessorMinecraft).session.username || minecraftAccount.username == (mc as AccessorMinecraft).session.username
            ) {
                index = i
                break
            }
        }
        altsList!!.elementClicked(index, false, 0, 0)
        altsList!!.scrollBy(index * altsList!!.slotHeight)
        val startPos = 22
        buttonList.add(GuiButton(1, width - 80, startPos + 24, 70, 20, "Add"))
        buttonList.add(GuiButton(2, width - 80, startPos + 24 * 2, 70, 20, "Remove"))
        buttonList.add(GuiButton(7, width - 80, startPos + 24 * 3, 70, 20, "Reload"))
        buttonList.add(GuiButton(8, width - 80, startPos + 24 * 4, 70, 20, "Copy"))
        buttonList.add(GuiButton(0, width - 80, height - 65, 70, 20, "Back"))
        buttonList.add(GuiButton(3, 5, startPos + 24, 90, 20, "Login").also { loginButton = it })
        buttonList.add(GuiButton(4, 5, startPos + 24 * 2, 90, 20, "Pick Random Alt").also { randomAltButton = it })
        buttonList.add(GuiButton(89, 5, startPos + 24 * 3, 90, 20, "Random Alt"))
        buttonList.add(GuiButton(6, 5, startPos + 24 * 4, 90, 20, "DirectLogin"))
        Keyboard.enableRepeatEvents(true)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        altsList!!.drawScreen(mouseX, mouseY, partialTicks)
        drawCenteredString(
            "AltManager",
            width / 2f,
            (height / 8f - getHeight(1f) * 5f - 15f), ColorRGB(Color.WHITE.rgb),
            1f,
            true
        )
        drawCenteredString(
            "Alts: " + +altAccounts.size,
            width / 2f,
            (height / 8 - getHeight(1f) * 5f - 4f),
            ColorRGB(Color.WHITE.rgb),
            1f,
            true
        )
        drawCenteredString(
            status!!,
            width / 2f,
            (height / 8f - getHeight(1f) * 5f + 8f),
            ColorRGB(Color.WHITE.rgb),
            1f,
            true
        )
        MainFontRenderer.drawString("Username: " + mc.session.username, 6f, 6f, ColorRGB(Color.WHITE.rgb), 1f, true)
        MainFontRenderer.drawString(
            "Type: " + if (isValidTokenOffline(mc.session.token)) "Premium" else "Cracked",
            6f,
            15f,
            ColorRGB(Color.WHITE.rgb),
            1f,
            true
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun onGuiClosed() {
        MainMenu.notReset = true
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(previousScreen)
            1 -> mc.displayGuiScreen(AltAddAccount(this))
            2 -> status = if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.size) {
                altAccounts.removeAt(altsList!!.getSelectedSlot())
                "Removed"
            } else "Please Select"
            3 -> if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.size) {
                randomAltButton!!.enabled = false
                loginButton!!.enabled = randomAltButton!!.enabled
                val minecraftAccount = altAccounts[altsList!!.getSelectedSlot()]
                var loginAction: AltLoginAction
                AltLoginAction(minecraftAccount).also { loginAction = it }.start()
                status = loginAction.status
                randomAltButton!!.enabled = true
                loginButton!!.enabled = randomAltButton!!.enabled
            } else status = "Please Select"
            4 -> {
                if (altAccounts.size <= 0) {
                    status = "Empty List"
                    return
                }
                val randomInteger = Random().nextInt(altAccounts.size)
                if (randomInteger < altsList!!.size) altsList!!.selectedSlot = randomInteger
                run {
                    randomAltButton!!.enabled = false
                    loginButton!!.enabled = randomAltButton!!.enabled
                }
                val minecraftAccount = altAccounts[altsList!!.getSelectedSlot()]
                var loginAction: AltLoginAction
                AltLoginAction(minecraftAccount).also { loginAction = it }.start()
                status = loginAction.status
                run {
                    randomAltButton!!.enabled = true
                    loginButton!!.enabled = randomAltButton!!.enabled
                }
            }
            6 -> mc.displayGuiScreen(AltDirectLogin(this))
            7 -> loadAccount()
            8 -> status = if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.size) {
                val altAccount = altAccounts[altsList!!.getSelectedSlot()]
                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(altAccount.username + ":" + altAccount.passwords),
                    null
                )
                "Copied"
            } else "Please Select"
            89 -> run { AltManager.randomCracked() }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                mc.displayGuiScreen(previousScreen)
                return
            }
            Keyboard.KEY_UP -> {
                var i = altsList!!.getSelectedSlot() - 1
                if (i < 0) i = 0
                altsList!!.elementClicked(i, false, 0, 0)
            }
            Keyboard.KEY_DOWN -> {
                var i = altsList!!.getSelectedSlot() + 1
                if (i >= altsList!!.size) i = altsList!!.size - 1
                altsList!!.elementClicked(i, false, 0, 0)
            }
            Keyboard.KEY_RETURN -> {
                altsList!!.elementClicked(altsList!!.getSelectedSlot(), true, 0, 0)
            }
            Keyboard.KEY_NEXT -> {
                altsList!!.scrollBy(height - 100)
            }
            Keyboard.KEY_PRIOR -> {
                altsList!!.scrollBy(-height + 100)
                return
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        altsList!!.handleMouseInput()
    }

    inner class GuiList internal constructor(prevGui: GuiScreen) :
        GuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {
        var selectedSlot = 0
        override fun isSelected(id: Int): Boolean {
            return selectedSlot == id
        }

        @JvmName("getSelectedSlot1")
        fun getSelectedSlot(): Int {
            if (selectedSlot > altAccounts.size) selectedSlot = -1
            return selectedSlot
        }

        public override fun getSize(): Int {
            return altAccounts.size
        }

        public override fun elementClicked(var1: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = var1
            if (doubleClick) {
                if (altsList!!.getSelectedSlot() != -1 && altsList!!.getSelectedSlot() < altsList!!.size && loginButton!!.enabled) {
                    randomAltButton!!.enabled = false
                    loginButton!!.enabled = randomAltButton!!.enabled
                    val loginAction = AltLoginAction(altAccounts[altsList!!.getSelectedSlot()])
                    loginAction.start()
                    Thread { while (!loginAction.loggingIn) status = loginAction.status }.start()
                    randomAltButton!!.enabled = true
                    loginButton!!.enabled = randomAltButton!!.enabled
                } else status = "Please select a account"
            }
        }

        override fun drawSlot(
            slotIndex: Int,
            xPos: Int,
            yPos: Int,
            heightIn: Int,
            mouseXIn: Int,
            mouseYIn: Int,
            partialTicks: Float
        ) {
            val minecraftAccount = altAccounts[slotIndex]
            drawCenteredString(
                minecraftAccount.username,
                width / 2f,
                (yPos + 2).toFloat(),
                ColorRGB(Color.WHITE.rgb),
                1f,
                true
            )
            drawCenteredString(
                if (minecraftAccount.isCracked) "Cracked" else "Premium",
                width / 2f,
                (yPos + 15).toFloat(),
                if (minecraftAccount.isCracked) ColorRGB(128, 128, 128) else ColorRGB(0, 255, 0),
                1f,
                true
            )
        }

        override fun drawBackground() {}
    }
}