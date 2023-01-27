package club.eridani.epsilon.loader.util

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.zip.Inflater
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


object AES {

    var secretKey: SecretKeySpec? = null

    inline fun setKey(myKey: String) {
        val sha: MessageDigest
        try {
            var key: ByteArray = myKey.toByteArray(StandardCharsets.UTF_8)
            sha = MessageDigest.getInstance("SHA-1")
            key = sha.digest(key)
            key = key.copyOf(16)
            secretKey = SecretKeySpec(key, "AES")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    inline fun String.encrypt(secret: String): String {
        try {
            setKey(secret)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            return Base64.getEncoder().encodeToString(cipher.doFinal(this.toByteArray(StandardCharsets.UTF_8)))
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return "null"
    }

    inline fun String.decrypt(secret: String): String? {
        try {
            setKey(secret)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return String(cipher.doFinal(Base64.getDecoder().decode(this)))
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }

    inline fun decrypt(strToDecrypt: ByteArray?, secret: String): ByteArray {
        try {
            setKey(secret)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            return cipher.doFinal(strToDecrypt)
        } catch (e: Exception) {
            println("Error while decrypting: $e")
        }
        return byteArrayOf()
    }

    inline fun decompress(input: ByteArray?, GZIPFormat: Boolean): ByteArray? {

        // Create an Inflater object to compress the data
        val decompressor = Inflater(GZIPFormat)

        // Set the input for the decompressor
        decompressor.setInput(input)

        // Decompress data
        val bao = ByteArrayOutputStream()
        val readBuffer = ByteArray(1024)
        while (!decompressor.finished()) {
            val readCount = decompressor.inflate(readBuffer)
            if (readCount > 0) {
                // Write the data to the output stream
                bao.write(readBuffer, 0, readCount)
            }
        }

        // End the decompressor
        decompressor.end()
        // Return the written bytes from the output stream
        return bao.toByteArray()
    }
}