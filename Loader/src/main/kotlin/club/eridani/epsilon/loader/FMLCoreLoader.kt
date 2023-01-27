package club.eridani.epsilon.loader

import club.eridani.epsilon.client.launch.InitHook
import club.eridani.epsilon.loader.MixinTool.getMixins
import club.eridani.epsilon.loader.MixinTool.getRefMapFile
import club.eridani.epsilon.loader.verify.LoaderConstants
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("EpsilonFMLLoader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class FMLCoreLoader : IFMLLoadingPlugin {

    var isObfuscatedEnvironment = false

    init {
        launch()
        if (LoaderConstants.shouldInit) {
            InitHook().init(
                LoaderClassLoader.array!![0] xor LoaderClassLoader.array!![1],
                LoaderConstants.refmapBytes.getRefMapFile(),
                LoaderConstants.mixinBytes.getMixins()
            )
        }
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
