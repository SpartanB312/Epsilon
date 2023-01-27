package club.eridani.epsilon.client.launch

import club.eridani.epsilon.client.gui.SpartanScreen
import club.eridani.epsilon.client.management.ModuleManager
import club.eridani.epsilon.client.module.client.RootGUI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import kotlin.reflect.jvm.jvmName

class InitHook {

    companion object {
        val log: Logger = LogManager.getLogger("Epsilon")
        lateinit var mixinRefmapFile: File
        lateinit var mixinCache: List<String>
    }

    //2221995
    fun init(data: Int, mixinRefmapFile: File, mixinCache: List<String>) {
        Companion.mixinRefmapFile = mixinRefmapFile
        Companion.mixinCache = mixinCache
        runBlocking {
            runBlocking {
                launch(Dispatchers.IO) {
                    log.info("Loading Epsilon ModLauncher")
                    when (data) {
                        69420 shl 10 xor 666 or 10 -> {
                            ModuleManager.modules.add(RootGUI)
                            SpartanScreen()
                        }
                        114514 shl 5 shr 10 xor 999 -> {
                            SpartanScreen()
                            ModuleManager.hudModules.removeAt(10)
                        }
                        69420 shl 10 shr 5 xor 555 -> {
                            if (Class.forName(InitManager::class.jvmName) != null) {
                                launch(Dispatchers.IO) {
                                    log.info("Loading Epsilon MixinLoader")
                                    MixinLoader.load()
                                }
                                return@launch
                            }
                        }
                        else -> {
                            log.info("Load Epsilon MixinLoader Successfully")
                            (init(data, mixinRefmapFile, mixinCache))
                        }
                    }
                }
            }
        }
    }

}