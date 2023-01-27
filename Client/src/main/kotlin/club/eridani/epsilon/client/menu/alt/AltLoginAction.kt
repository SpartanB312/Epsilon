package club.eridani.epsilon.client.menu.alt

import club.eridani.epsilon.client.management.AltManager.loginCracked
import club.eridani.epsilon.client.mixin.mixins.accessor.AccessorMinecraft
import com.mojang.authlib.Agent
import com.mojang.authlib.exceptions.AuthenticationException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import net.minecraft.client.Minecraft
import net.minecraft.util.Session
import java.net.Proxy

class AltLoginAction(alt: AltAccount) : Thread() {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private val account: AltAccount = alt
    var status = "Waiting..."
    var loggingIn = false
    private fun createSession(username: String?, password: String?): Session? {
        val service = YggdrasilAuthenticationService(Proxy.NO_PROXY, "")
        val auth = service.createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication
        auth.setUsername(username)
        auth.setPassword(password)
        return try {
            auth.logIn()
            Session(auth.selectedProfile.name, auth.selectedProfile.id.toString(), auth.authenticatedToken, "mojang")
        } catch (localAuthenticationException: AuthenticationException) {
            localAuthenticationException.printStackTrace()
            null
        }
    }

    override fun run() {
        if (account.username == "") {
            status = "Please Enter username"
            return
        }
        status = "Logging in..."
        if (account.passwords == "" || account.passwords!!.isEmpty()) {
            status = "Logged in with cracked : " + account.username
            loginCracked(account.username)
            loggingIn = true
            return
        }
        val session = createSession(account.username, account.passwords)
        if (session == null) {
            status = "Login failed!"
        } else {
            try {
                (mc as AccessorMinecraft).session = session
                status = "Logged in : " + session.username
            } catch (ignored: Exception) {
            }
        }
        loggingIn = true
    }

}