package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.Epsilon.MOD_NAME
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Suppress("NOTHING_TO_INLINE")
object Logger {
    @JvmStatic
    val logger: Logger = LogManager.getLogger(MOD_NAME)

    @JvmStatic
    inline fun info(string: String) {
        logger.info(string)
    }

    @JvmStatic
    inline fun warn(string: String) {
        logger.warn(string)
    }

    @JvmStatic
    inline fun error(string: String) {
        logger.error(string)
    }

    @JvmStatic
    inline fun fatal(string: String) {
        logger.fatal(string)
    }
}