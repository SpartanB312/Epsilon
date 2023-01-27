package club.eridani.epsilon.client

import club.eridani.epsilon.client.event.EventBus
import club.eridani.epsilon.client.management.ModuleManager
import club.eridani.epsilon.client.util.Wrapper.mc
import kotlinx.coroutines.runBlocking
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.security.MessageDigest
import javax.swing.JOptionPane
import javax.xml.bind.DatatypeConverter

class Auth {

    private val socket = Socket("139.99.88.2", 9900)
    var receivedMessage = "null"
    val hardwareID = (System.getenv("COMPUTERNAME") + System.getenv("HOMEDRIVE") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version") + Runtime.getRuntime().availableProcessors() + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS")).sha1().sha256().sha512().sha1().sha256()
    var isReceived = false

    init {
        runCatching {
            Class.forName("a.z")
            val input = DataInputStream(socket.getInputStream())
            val output = DataOutputStream(socket.getOutputStream())

            output.writeUTF("[HWIDSEC]$hardwareID")

            val authMessage = input.readUTF()
            isReceived = true
            socket.close()

            if (authMessage.equals(hardwareID.sha1())) {
                receivedMessage = authMessage
            } else {
                mc.fontRenderer = null
                showExceptionAndCrash(authMessage)
            }
        }.onFailure {
            showExceptionAndCrash("?")
        }
    }

    inline fun showExceptionAndCrash(crash: String) {
        val optionPane = JOptionPane(crash, JOptionPane.WARNING_MESSAGE)
        val dialog = optionPane.createDialog("Verify Failed!")
        dialog.isAlwaysOnTop = true
        dialog.isVisible = true

        ModuleManager.hudModules.forEach {
            it.name = "ZealotCrystal"
        }
        ModuleManager.modules.forEach {
            it.name = "ZealotCrystal"
        }

        runBlocking {
            mc.world = null
            mc.player = null
        }


        EventBus.registered.clear()
        EventBus.registeredParallel.clear()
        EventBus.subscribed.clear()
        EventBus.subscribedParallel.clear()
    }
}



fun String.sha1(): String = hashString("SHA-1", this)
fun String.sha256(): String = hashString("SHA-256", this)
fun String.sha512(): String = hashString("SHA-512", this)

inline fun hashString(type: String, input: String): String {
    val bytes = MessageDigest.getInstance(type).digest(input.toByteArray())
    return DatatypeConverter.printHexBinary(bytes).toLowerCase()
}