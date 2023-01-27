package club.eridani.epsilon.client.util.text

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.util.Utils
import net.minecraft.util.text.TextComponentString

object ChatUtil : Helper {

    private const val DELETE_ID = 94423

    const val SECTION_SIGN = '§'

    const val DARK_RED = "${SECTION_SIGN}4"
    const val RED = "${SECTION_SIGN}c"
    const val GOLD = "${SECTION_SIGN}6"
    const val YELLOW = "${SECTION_SIGN}e"
    const val DARK_GREEN = "${SECTION_SIGN}2"
    const val GREEN = "${SECTION_SIGN}a"
    const val AQUA = "${SECTION_SIGN}b"
    const val DARK_AQUA = "${SECTION_SIGN}3"
    const val DARK_BLUE = "${SECTION_SIGN}1"
    const val BLUE = "${SECTION_SIGN}9"
    const val LIGHT_PURPLE = "${SECTION_SIGN}d"
    const val DARK_PURPLE = "${SECTION_SIGN}5"
    const val WHITE = "${SECTION_SIGN}f"
    const val GRAY = "${SECTION_SIGN}7"
    const val DARK_GRAY = "${SECTION_SIGN}8"
    const val BLACK = "${SECTION_SIGN}0"

    const val RESET = "${SECTION_SIGN}r"
    const val OBFUSCATED = "${SECTION_SIGN}k"
    const val STRIKE_THROUGH = "${SECTION_SIGN}m"
    const val UNDER_LINE = "${SECTION_SIGN}n"
    const val ITALIC = "${SECTION_SIGN}o"

    fun sendNoSpamMessage(message: String, messageID: Int) {
        sendNoSpamRawChatMessage(
            SECTION_SIGN + "7[" + SECTION_SIGN + "9" + Epsilon.MOD_NAME + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message,
            messageID
        )
    }

    fun sendNoSpamMessage(message: String) {
        sendNoSpamRawChatMessage(SECTION_SIGN + "7[" + SECTION_SIGN + "9" + Epsilon.MOD_NAME + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message)
    }

    fun sendNoSpamMessage(messages: Array<String?>) {
        sendNoSpamMessage("")
        for (s in messages) sendNoSpamRawChatMessage(s)
    }

    fun sendNoSpamWarningMessage(message: String) {
        sendNoSpamRawChatMessage(SECTION_SIGN + "7[" + SECTION_SIGN + "e" + "Warning" + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message)
    }

    fun sendNoSpamWarningMessage(message: String, messageID: Int) {
        sendNoSpamRawChatMessage(
            SECTION_SIGN + "7[" + SECTION_SIGN + "e" + "Warning" + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message,
            messageID
        )
    }

    fun sendNoSpamErrorMessage(message: String) {
        sendNoSpamRawChatMessage(SECTION_SIGN + "7[" + SECTION_SIGN + "4" + SECTION_SIGN + "lERROR" + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message)
    }

    fun sendNoSpamErrorMessage(message: String, messageID: Int) {
        sendNoSpamRawChatMessage(
            SECTION_SIGN + "7[" + SECTION_SIGN + "4" + SECTION_SIGN + "lERROR" + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message,
            messageID
        )
    }

    fun sendNoSpamRawChatMessage(message: String?) {
        sendSpamlessMessage(message)
    }

    fun sendNoSpamRawChatMessage(message: String?, messageID: Int) {
        sendSpamlessMessage(messageID, message)
    }

    fun printRawChatMessage(message: String?) {
        mc.addScheduledTask {
            if (!Utils.nullCheck()) {
                mc.ingameGUI.chatGUI.printChatMessage(TextComponentString(message))
            }
        }
    }

    fun printChatMessage(message: String) {
        printRawChatMessage(SECTION_SIGN + "7[" + SECTION_SIGN + "9" + Epsilon.MOD_NAME + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message)
    }

    fun printErrorChatMessage(message: String) {
        printRawChatMessage(SECTION_SIGN + "7[" + SECTION_SIGN + "4ERROR" + SECTION_SIGN + "7] " + SECTION_SIGN + "r" + message)
    }

    fun sendSpamlessMessage(message: String?) {
        mc.addScheduledTask {
            if (!Utils.nullCheck()) {
                mc.ingameGUI.chatGUI
                    .printChatMessageWithOptionalDeletion(TextComponentString(message), DELETE_ID)
            }
        }
    }

    fun sendSpamlessMessage(messageID: Int, message: String?) {
        mc.addScheduledTask {
            if (!Utils.nullCheck()) {
                mc.ingameGUI.chatGUI
                    .printChatMessageWithOptionalDeletion(TextComponentString(message), messageID)
            }
        }
    }
}