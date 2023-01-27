package club.eridani.epsilon.debug

import club.eridani.epsilon.client.launch.InitHook
import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("EpsilonFMLLoader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class FMLCoreMod : IFMLLoadingPlugin {

    var isObfuscatedEnvironment = false
    private val TEMP_DIR: String = System.getProperty("java.io.tmpdir")
    private val nextTempFile get() = File(TEMP_DIR, "+~JF${randomString(18)}" + ".tmp")
    private fun randomString(length: Int): String {
        val allowedChars = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    init {
        val refMapFile = nextTempFile.apply {
            try {
                FileOutputStream(this).let {
                    it.write(FMLCoreMod::class.java.getResourceAsStream("/mixins.epsilon.refmap.json")!!.readBytes())
                    it.flush()
                    it.close()
                    this.deleteOnExit()
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        val mixinCache = mutableListOf<String>()

        Gson().fromJson(
            String(
                FMLCoreMod::class.java.getResourceAsStream("/mixins.epsilon.json")!!.readBytes(),
                StandardCharsets.UTF_8
            ),
            JsonObject::class.java
        ).apply {
            getAsJsonArray("client")?.forEach {
                mixinCache.add(it.asString)
            }
            getAsJsonArray("mixins")?.forEach {
                mixinCache.add(it.asString)
            }
        }

        InitHook().init(2221995, refMapFile, mixinCache)
    }

    override fun getModContainerClass(): String? = null

    override fun getASMTransformerClass(): Array<String> = emptyArray()

    override fun getSetupClass(): String? = null

    override fun injectData(data: Map<String?, Any?>) {
        isObfuscatedEnvironment = (data["runtimeDeobfuscationEnabled"] as Boolean?)!!
    }

    override fun getAccessTransformerClass(): String? {
        return null
    }

}
