package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.menu.alt.AltAccount
import club.eridani.epsilon.client.mixin.mixins.accessor.AccessorMinecraft
import com.google.gson.JsonParser
import net.minecraft.util.Session
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt


object AltManager : Helper {
    val altAccounts = mutableListOf<AltAccount>()
    private const val ACCOUNT_CONFIG = Epsilon.DEFAULT_CONFIG_PATH + "config/" + "EpsilonAccount.json"

    fun loadAccount() {
        if (!File(ACCOUNT_CONFIG).exists()) {
            saveAccount()
        } else {
            try {
                FileReader(ACCOUNT_CONFIG).readLines().forEach { line ->
                    val account = line.split(":")
                    var exist = false
                    altAccounts.forEach {
                        if (it.username == account[0]) exist = true
                    }
                    if (!exist)
                        addAccount(AltAccount(account[0], account[1]))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun addAccount(account: AltAccount) {
        altAccounts.add(account)
    }

    fun saveAccount() {
        try {
            creatFile(ACCOUNT_CONFIG)
            val writer = BufferedWriter(FileWriter(ACCOUNT_CONFIG, false))
            altAccounts.forEach {
                println("${it.username}:${it.passwords}")
                writer.write("${it.username}:${it.passwords}")
                writer.flush()
                writer.newLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun creatFile(config: String) {
        if (!File(config).exists()) {
            File(config).parentFile.mkdirs()
            try {
                File(config).createNewFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }
    }

    private val iChar = arrayOf("1", "i", "I", "l")
    private val oChar = arrayOf("o", "O", "0")
    private val countries = arrayOf("JP", "CN", "BR", "ES", "US", "UK", "TW", "HK", "KR", "FR")

    fun isValidTokenOffline(token: String) = token.length >= 32

    fun randomCracked() {
        fun getRandomInt() = (0..100).random()
        fun getRandomNameWord() = "Epsilon"
        fun getRandomCountry() = countries.random()

        val random1 = getRandomInt()
        // generate name base
        var name = when {
            random1 < 20 -> {
                "Xx${getRandomNameWord().uppercase(Locale.getDefault())}xX"
            }
            random1 < 40 -> {
                "X${getRandomNameWord().lowercase(Locale.getDefault())}X"
            }
            random1 < 60 -> {
                "${getRandomNameWord()}Zin"
            }
            random1 < 80 -> {
                "${getRandomNameWord()}GOD"
            }
            random1 < 95 -> {
                getRandomNameWord()
            }
            else -> { // OHHH THIS IS A EGG
                val sb = StringBuilder()
                if (Math.random() > 0.5) {
                    repeat((4..12).random()) {
                        sb.append(iChar[(Math.random() * iChar.size).roundToInt()])
                    }
                } else {
                    repeat((4..13).random()) {
                        sb.append(oChar[(Math.random() * oChar.size).roundToInt()])
                    }
                }
                sb.toString()
            }
        }

        // change chars to pvp player liked
        val random2 = getRandomInt()
        val sb = StringBuilder()
        for (char in name.toCharArray()) {
            if (random2 < 30) {
                if (Math.random() > 0.5) {
                    sb.append(char.lowercaseChar())
                } else {
                    sb.append(char.uppercaseChar())
                }
            } else if (random2 < 60 && Math.random() > 0.3) {
                sb.append(
                    when (char.lowercaseChar()) {
                        'i' -> '1'
                        'l' -> '1'
                        'e' -> '3'
                        'a' -> '4'
                        's' -> '5'
                        'o' -> '0'
                        else -> char
                    }
                )
            } else {
                sb.append(char)
            }
        }
        name = sb.toString()

        // add additions
        val random3 = getRandomInt()
        name += when {
            random3 < 15 -> {
                "_"
            }
            random3 < 30 -> {
                "__"
            }
            random3 < 45 -> {
                "_YT"
            }
            random3 < 55 -> {
                "_PVP"
            }
            random3 < 65 -> {
                getRandomCountry()
            }
            random3 < 75 -> {
                "_${getRandomCountry()}"
            }
            else -> {
                ""
            }
        }

        loginCracked(name)
    }

    fun loginCracked(username: String): Session {
        val session = Session(username, getUUID(username), "-", "legacy")
        (mc as AccessorMinecraft).setSession(session)
        return session
    }

    fun getUUID(username: String): String {
        try {
            // Make a http connection to Mojang API and ask for UUID of username
            val httpConnection =
                URL("https://api.mojang.com/users/profiles/minecraft/$username").openConnection() as HttpsURLConnection
            httpConnection.connectTimeout = 2000
            httpConnection.readTimeout = 2000
            httpConnection.requestMethod = "GET"
            httpConnection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"
            )
            HttpURLConnection.setFollowRedirects(true)
            httpConnection.doOutput = true

            if (httpConnection.responseCode != 200)
                return ""

            // Read response content and get id from json
            InputStreamReader(httpConnection.inputStream).use {
                val jsonElement = JsonParser().parse(it)

                if (jsonElement.isJsonObject) {
                    return jsonElement.asJsonObject.get("id").asString
                }
            }
        } catch (ignored: Throwable) {
        }

        return ""
    }
}