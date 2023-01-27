package club.eridani.epsilon.loader

import club.eridani.epsilon.loader.ui.MenuLauncher
import club.eridani.epsilon.loader.util.AES.decompress
import club.eridani.epsilon.loader.util.AES.decrypt
import club.eridani.epsilon.loader.util.AES.encrypt
import club.eridani.epsilon.loader.util.NoStackTraceThrowable
import club.eridani.epsilon.loader.util.Util
import club.eridani.epsilon.loader.verify.LoaderConstants
import kotlinx.coroutines.DelicateCoroutinesApi
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.util.*
import java.util.zip.Deflater
import java.util.zip.ZipInputStream

val logger: Logger = LogManager.getLogger("Epsilon")

@OptIn(DelicateCoroutinesApi::class)
class LoaderClassLoader(private val host: String) {

    companion object {
        var array: IntArray? = null
    }

    private val hardwareID = (System.getenv("COMPUTERNAME") + System.getenv("HOMEDRIVE") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version") + Runtime.getRuntime().availableProcessors() + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS")).sha1().sha256().sha512().sha1().sha256()


    private val resourceCache = LaunchClassLoader::class.java.getDeclaredField("resourceCache").let {
        it.isAccessible = true
        it[Launch.classLoader] as MutableMap<String, ByteArray>
    }

    private var lastStartTime = 0L
    private var flag = false
    private var startDownload = false

    private val mainThread = Thread.currentThread()
    private var downloadJob = DownloadJob(this, mainThread)

    init {
        Thread {
            while (!LoaderConstants.shouldInit) {
                if (startDownload) {
                    if (System.currentTimeMillis() - lastStartTime >= 5000 && flag) {
                        if (downloadJob.started && !downloadJob.finished) {
                            logger.fatal("Epsilon classes are broken, Try to download again")
                            downloadJob.stop()
                            downloadJob = DownloadJob(this, mainThread)
                            downloadJob.start()
                        }
                    }
                }
                Thread.sleep(1)
            }
        }.start()

        downloadJob.start()
        mainThread.suspend()
    }

    private class DownloadJob(private val loaderClassLoader: LoaderClassLoader, private val needResume: Thread?) :
        Thread() {
        @Volatile
        var started = false

        @Volatile
        var finished = false

        override fun run() {
            loaderClassLoader.download()
            finished = true
            needResume?.resume()
        }

        override fun start() {
            started = true
            super.start()
        }
    }

    private fun download() {

        startDownload = false
        val fileSocket = Socket(host, 6791)
        val inputF = DataInputStream(fileSocket.getInputStream())
        val outputF = DataOutputStream(fileSocket.getOutputStream())


        val key = UUID.randomUUID().toString()
        outputF.writeUTF("[ENCRYPT]$key")
        outputF.writeUTF("[ACCOUNT]" + "${LoaderConstants.user.username}:${LoaderConstants.user.password}:$hardwareID".encrypt(key.sha512().sha256().sha1()))

        val input: String = inputF.readUTF()

        if (input.decrypt(key.sha512().sha256().sha1().sha256().sha512()) == "[PASSED]") {
            val latestVersion = inputF.readUTF()
            val first = inputF.readUTF() // if u dont want my eskid LoaderSupport u can just remove this
            val second = inputF.readUTF() // if u dont want my eskid LoaderSupport u can just remove this

            val initP = inputF.readInt()
            val initP2 = inputF.readInt()
            array = intArrayOf(initP, initP2)

            try {
                if (LOADER_VERSION.toFloat() >= latestVersion.toFloat()) {
                    logger.info("Loading Epsilon classes...")

                    ZipInputStream(BufferedInputStream(inputF)).use { zipStream ->

                        startDownload = true

                        while (true) {
                            val zipEntry = zipStream.nextEntry

                            if (zipEntry == null) break
                            else if (zipEntry.name.endsWith(".WNCRY", true)
                                || zipEntry.name.endsWith(".WNCRYT", true)
                                || zipEntry.name.endsWith(".class", true)) {
                                lastStartTime = System.currentTimeMillis()
                                flag = true

                                resourceCache[zipEntry.name.removeSuffix(".WNCRY").removeSuffix(".WNCRYT").removeSuffix(".class").replace('/', '.')] =
                                    decrypt(
                                        if (zipEntry.name.endsWith(".WNCRYT"))
                                            decompress(zipStream.readBytes(), false)
                                        else
                                            zipStream.readBytes(),
                                        (first.sha1().sha512() + second.sha256().sha1()).sha1()
                                                + second.sha256().sha1()
                                                + first.sha1().sha512()
                                    )

                                flag = false
                            } else {
                                lastStartTime = System.currentTimeMillis()
                                flag = true
                                if (zipEntry.name == "mixins.epsilon.json") {
                                    LoaderConstants.mixinBytes = zipStream.readBytes()
                                } else if (zipEntry.name == "mixins.epsilon.refmap.json") {
                                    LoaderConstants.refmapBytes = zipStream.readBytes()
                                }
                                flag = false
                            }
                        }
                    }

                    logger.info("Loaded Epsilon successfully!")
                    LoaderConstants.shouldInit = true
                } else {
                    logger.warn("Loader outdated!")
                    LoaderConstants.shouldInit = false
                    if (!fileSocket.isClosed) fileSocket.close()
                    Util.showDialog("Your loader version is outdated. \nPlease update to latest version (v$input)", "WARNING")
                    val shutDownMethod = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", Integer.TYPE)
                    shutDownMethod.isAccessible = true
                    shutDownMethod.invoke(null, 0)
                }
            } catch (e: Exception) {
                Util.showDialog("Failed to load.", "ERROR")
                runCatching {
                    val shutDownMethod = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", Integer.TYPE)
                    shutDownMethod.isAccessible = true
                    shutDownMethod.invoke(null, 0)
                }.onFailure {
                    LoaderConstants.shouldInit = false
                    throw NoStackTraceThrowable("Failed to load.")
                }.onSuccess {
                    LoaderConstants.shouldInit = false
                }
            }
        } else {
            fileSocket.close()
            MenuLauncher.displayLoginMenu(false, Thread.currentThread())
            Thread.currentThread().suspend()
        }
    }

}