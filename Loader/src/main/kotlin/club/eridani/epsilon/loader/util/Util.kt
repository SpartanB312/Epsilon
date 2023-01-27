package club.eridani.epsilon.loader.util

import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.swing.ImageIcon
import javax.swing.JOptionPane
import javax.xml.bind.DatatypeConverter

object Util {

    fun showDialog(s: String, title: String) {
        val optionPane = JOptionPane(s, JOptionPane.WARNING_MESSAGE)
        val dialog = optionPane.createDialog(title)
        val icon = ImageIcon(Util::class.java.getResource("/assets/epsilon/icon/ie32.png"))
        dialog.setIconImage(icon.image)
        dialog.isAlwaysOnTop = true
        dialog.isVisible = true
    }

    inline fun File.writeToFile(byte: ByteArray) {
        FileOutputStream(this).use {
            it.write(byte)
            it.flush()
            it.close()
        }
    }

    fun String.isEmptyOrBlank() = this.isBlank() or this.isEmpty()

    fun String.isInvalid() = this.contains(" ") || this.contains(":")

    fun String.tooLong() = this.length > 100

    inline fun getRandomString(length: Int): String {
        val allowedChars = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    inline fun hashString(type: String, input: String): String {
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        return DatatypeConverter.printHexBinary(bytes).toLowerCase()
    }
//
//    inline fun getBestServer(): String {
//        val ping1 = pingServer("killred.club")
//        val ping2 = pingServer("111.67.193.37")
//        return if (ping2 >= ping1) "killred.club" else "111.67.193.37"
//    }
//
//    inline fun pingServer(host: String): Long {
//        var ping = 9000L
//        val startTime = System.currentTimeMillis()
//        val fileSocket = Socket(host, 23334)
//        val inputF = DataInputStream(fileSocket.getInputStream())
//        val outputF = DataOutputStream(fileSocket.getOutputStream())
//        outputF.writeUTF("PINGTEST")
//        if (inputF.readUTF() == "OK") {
//            ping = System.currentTimeMillis() - startTime
//        }
//        if (!fileSocket.isClosed)
//            fileSocket.close()
//        return ping
//    }

    inline fun xor(array: ByteArray, key: Int): ByteArray {
        val bytes = ByteArray(array.size)
        for (i in array.indices) {
            bytes[i] = (array[i].toInt() xor key).toByte()
        }
        return bytes
    }
}