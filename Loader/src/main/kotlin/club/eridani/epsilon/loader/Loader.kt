package club.eridani.epsilon.loader

import club.eridani.epsilon.loader.ui.MenuLauncher.displayLoginMenu
import club.eridani.epsilon.loader.util.Util.hashString
import club.eridani.epsilon.loader.util.Util.isEmptyOrBlank
import club.eridani.epsilon.loader.verify.LoaderConstants
import club.eridani.epsilon.loader.verify.UserdataHelper

/**
 * @author cookiedragon234 31/Mar/2020
 */

const val LOADER_VERSION = "0.4"

fun main() {
    displayLoginMenu(true, null)
}

fun launch() {
//    noMelonUser()
    val account = UserdataHelper.loadConfig()

    if (account != null) {
        val user = account.split("-")
        LoaderConstants.user.username = user[0]
        LoaderConstants.user.password = user[1]
    }

    if (LoaderConstants.user.username.isEmptyOrBlank() || LoaderConstants.user.password.isEmptyOrBlank()) {
        displayLoginMenu(false, Thread.currentThread())
        Thread.currentThread().suspend()
    }

    LoaderClassLoader("139.99.88.2")
}

fun String.sha1(): String = hashString("SHA-1", this)
fun String.sha256(): String = hashString("SHA-256", this)
fun String.sha512(): String = hashString("SHA-512", this)