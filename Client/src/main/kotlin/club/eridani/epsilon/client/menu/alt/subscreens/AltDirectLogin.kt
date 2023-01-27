package club.eridani.epsilon.client.menu.alt.subscreens

import club.eridani.epsilon.client.management.AltManager.addAccount
import club.eridani.epsilon.client.management.AltManager.altAccounts
import club.eridani.epsilon.client.menu.alt.AltAccount
import club.eridani.epsilon.client.menu.alt.AltLoginAction
import club.eridani.epsilon.client.module.setting.MenuSetting
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class AltDirectLogin(private val previousScreen: GuiScreen) : GuiScreen() {
    private var username: GuiTextField? = null
    private var password: PasswordField? = null
    private var loginAction: AltLoginAction? = null
    private var status = "Waiting..."
    override fun initGui() {
        val var3 = height / 4 + 24
        val buttonLogin = GuiButton(0, width / 2 - 100, var3 + 72 + 12, "Login")
        val clipboardButton = GuiButton(2, width / 2 - 100, var3 + 72 + 12 + buttonLogin.height + 2, "Clipboard Login")
        val buttonBack =
            GuiButton(1, width / 2 - 100, var3 + 72 + 12 + buttonLogin.height + clipboardButton.height + 4, "Back")
        buttonList.add(buttonLogin)
        buttonList.add(buttonBack)
        buttonList.add(clipboardButton)
        username = GuiTextField(var3, mc.fontRenderer, width / 2 - 100, 60, 200, 20)
        password = PasswordField(
            var3,
            mc.fontRenderer,
            width / 2 - 100,
            100,
            200,
            20
        )
        username!!.isFocused = true
        Keyboard.enableRepeatEvents(true)
    }

    override fun drawScreen(p_drawScreen_1_: Int, p_drawScreen_2_: Int, p_drawScreen_3_: Float) {
        MenuSetting.texture!!.bindTexture()
        drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, width, height, width.toFloat(), height.toFloat())
        MenuSetting.texture!!.unbindTexture()
        drawRect(0, 0, width, height, Color(0, 0, 0, 70).rgb)
        username!!.drawTextBox()
        password!!.drawTextBox()
        MainFontRenderer.drawCenteredString(
            if (loginAction == null) status else loginAction!!.status,
            width / 2f,
            (height / 8f - getHeight(1.5f) - 5f),
            ColorRGB(Color.WHITE.rgb),
            1.5f,
            true
        )
        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            2 -> {
                try {
                    val clipboardData = Toolkit.getDefaultToolkit().systemClipboard
                        .getData(DataFlavor.stringFlavor) as String
                    val accountData = clipboardData.split(":").toTypedArray()
                    if (!clipboardData.contains(":") || accountData.size != 2) {
                        status = "InvalidClipData"
                        return
                    }
                    var isExist = false
                    for (account in altAccounts) {
                        if (account.username == accountData[0]) {
                            isExist = true
                            break
                        }
                    }
                    AltLoginAction(AltAccount(accountData[0], accountData[1])).also { loginAction = it }.start()
                    if (!isExist && loginAction!!.status != "Login failed!") {
                        addAccount(AltAccount(accountData[0], accountData[1]))
                    }
                } catch (e: UnsupportedFlavorException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            1 -> {
                mc.displayGuiScreen(previousScreen)
            }
            0 -> {
                var isExist = false
                for (account in altAccounts) {
                    if (account.username == username!!.text) {
                        isExist = true
                        break
                    }
                }
                AltLoginAction(AltAccount(username!!.text, password!!.getText())).also { loginAction = it }.start()
                if (!isExist && loginAction!!.status != "Login failed!") {
                    addAccount(AltAccount(username!!.text, password!!.getText()))
                }
            }
        }
    }

    override fun mouseClicked(x: Int, y: Int, button: Int) {
        try {
            super.mouseClicked(x, y, button)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        username!!.mouseClicked(x, y, button)
        password!!.mouseClicked(x, y, button)
    }

    override fun keyTyped(character: Char, key: Int) {
        super.keyTyped(character, key)

        if (key == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(previousScreen)
            return
        }
        if (character == '\t') {
            if (!username!!.isFocused && !password!!.isFocused()) {
                username!!.isFocused = true
            } else {
                username!!.isFocused = password!!.isFocused()
                password!!.setFocused(!username!!.isFocused)
            }
        }
        if (character == '\r') {
            actionPerformed(buttonList[0])
        }
        username!!.textboxKeyTyped(character, key)
        password!!.textboxKeyTyped(character, key)
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
    }

    override fun updateScreen() {
        username!!.updateCursorCounter()
        password!!.updateCursorCounter()
    }
}