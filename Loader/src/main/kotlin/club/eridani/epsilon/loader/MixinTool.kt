package club.eridani.epsilon.loader

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

object MixinTool {

    private val TEMP_DIR: String = System.getProperty("java.io.tmpdir")
    private val nextTempFile get() = File(TEMP_DIR, "+~JF${randomString(18)}" + ".tmp")
    private fun randomString(length: Int): String {
        val allowedChars = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun ByteArray.getRefMapFile(): File {
        return nextTempFile.apply {
            try {
                FileOutputStream(this).let {
                    it.write(this@getRefMapFile)
                    it.flush()
                    it.close()
                    this.deleteOnExit()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun ByteArray.getMixins(): List<String> {
        val mixinCache = mutableListOf<String>()
        Gson().fromJson(
            String(this, StandardCharsets.UTF_8),
            JsonObject::class.java
        ).apply {
            getAsJsonArray("client")?.forEach {
                mixinCache.add(it.asString)
            }
            getAsJsonArray("mixins")?.forEach {
                mixinCache.add(it.asString)
            }
        }
        return mixinCache
    }

}