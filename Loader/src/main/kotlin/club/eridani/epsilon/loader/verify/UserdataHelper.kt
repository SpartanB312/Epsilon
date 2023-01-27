package club.eridani.epsilon.loader.verify

import club.eridani.epsilon.loader.util.AES.decrypt
import club.eridani.epsilon.loader.util.AES.encrypt
import club.eridani.epsilon.loader.util.Util.isEmptyOrBlank
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object UserdataHelper {
    private val DIR = System.getProperty("user.home") + "/EpsilonLoader/"
    private val CONFIG = DIR + "user.json"

    fun loadConfig(): String? {
        val file = createConfig()

        try {
            val bufferedReader = BufferedReader(FileReader(file))
            val line = bufferedReader.readLine() ?: return null

            return if (line.isEmptyOrBlank()) {
                bufferedReader.close()
                null
            } else {
                val decrypt = line.decrypt("loader-gay") ?: return null

                bufferedReader.close()

                decrypt
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    fun saveConfig(isSelected: Boolean) {
        val pw = if (isSelected) LoaderConstants.user.password else ""
        val config =
            "${LoaderConstants.user.username}-$pw-$isSelected".encrypt(
                "loader-gay"
            )
        val file = createConfig()
        file.bufferedWriter().use { writer ->
            writer.write(config)
        }
    }

    private fun createConfig(): File {
        val file = File(CONFIG)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()

        }
        return file
    }
}