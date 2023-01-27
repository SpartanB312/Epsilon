package club.eridani.epsilon.client.launch

import club.eridani.epsilon.client.Epsilon


object InitManager {

    @JvmStatic
    fun onMinecraftInit() {

    }

    @JvmStatic
    fun onFinishingInit() {

    }

    @JvmStatic
    fun preInitHook() {
        Epsilon.preInit()
    }

    @JvmStatic
    fun postInitHook() {
        Epsilon.postInit()
    }

}