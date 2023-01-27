package club.eridani.epsilon.loader.ui

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme
import club.eridani.epsilon.loader.sha1
import club.eridani.epsilon.loader.sha256
import club.eridani.epsilon.loader.sha512
import club.eridani.epsilon.loader.util.Util.isEmptyOrBlank
import club.eridani.epsilon.loader.util.Util.isInvalid
import club.eridani.epsilon.loader.util.Util.showDialog
import club.eridani.epsilon.loader.util.Util.tooLong
import club.eridani.epsilon.loader.verify.LoaderConstants
import club.eridani.epsilon.loader.verify.User
import club.eridani.epsilon.loader.verify.UserdataHelper.loadConfig
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import javax.swing.JFrame

object MenuLauncher {
    var loginMenu: LoginMenu
    var chance = 5

    init {
        FlatOneDarkIJTheme.install()
        loginMenu = LoginMenu()
    }

    inline fun displayLoginMenu(exitOnClose: Boolean, mainThread: Thread?) {
        val account = loadConfig()

        if (account != null) {
            val user = account.split("-")
            val un = user[0]
            val pw = user[1]
            loginMenu.username.text = un
            loginMenu.password.text = pw
            LoaderConstants.user.username = un
            LoaderConstants.user.password = pw
            loginMenu.rememberMeCheckBox.isSelected = user[2].toBoolean()
        }

        loginMenu.isVisible = true
        if (exitOnClose) loginMenu.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val HWID = (System.getenv("COMPUTERNAME")
                + System.getenv("HOMEDRIVE")
                + System.getProperty("os.name")
                + System.getProperty("os.arch")
                + System.getProperty("os.version")
                + Runtime.getRuntime().availableProcessors()
                + System.getenv("PROCESSOR_LEVEL")
                + System.getenv("PROCESSOR_REVISION")
                + System.getenv("PROCESSOR_IDENTIFIER")
                + System.getenv("PROCESSOR_ARCHITECTURE")
                + System.getenv("PROCESSOR_ARCHITEW6432")
                + System.getenv("NUMBER_OF_PROCESSORS")).sha1().sha256().sha512().sha1().sha256()

        val fileSocket = Socket("139.99.88.2", 6791)


        if (!fileSocket.isClosed) {
            val inputF = DataInputStream(fileSocket.getInputStream())
            val outputF = DataOutputStream(fileSocket.getOutputStream())

            loginMenu.loginButton.addActionListener {
                val password = loginMenu.password.text
                val username = loginMenu.username.text

                if (detectString(username, "username")) {
                    return@addActionListener
                } else if (detectString(password, "password")) {
                    return@addActionListener
                }

                loginMenu.state.text = "Logging in..."
                outputF.writeUTF("[ACCOUNT_NO_GAME]$username:$password:$HWID")

                when (inputF.readUTF()) {
                    "[INCORRECT_USERNAME]" -> {
                        showDialog("Incorrect username, please register account with active code", "Login Failed!")
                        loginMenu.state.text = "Login Failed! Incorrect username"

                    }
                    "[INCORRECT_PASSWORD]" -> {
                        showDialog(
                            "Incorrect password, forgot password? you can contact KillRED#7392 to reset password",
                            "Login Failed!"
                        )
                        loginMenu.state.text = "Login Failed! Incorrect password"
                    }
                    "[INCORRECT_HWID]" -> {
                        showDialog(
                            "Incorrect HWID, new HWID detected. Please contact KillRED#7392 to reset HWID",
                            "Login Failed!"
                        )
                        loginMenu.state.text = "Login Failed! Incorrect HWID"
                    }
                    "[PASSED]" -> {
                        loginMenu.state.text = "Successful login!"
                        LoaderConstants.user = User(username, password, HWID)
                        if (!exitOnClose) {
                            loginMenu.isVisible = false
                            mainThread?.resume()
                        }
                    }
                }
            }

            loginMenu.activeButton.addActionListener {
                val password = loginMenu.regisPassword.text
                val username = loginMenu.regisUsername.text
                val activeCode = loginMenu.activeCode.text

                if (chance == 0) {
                    showDialog("stop trying nigga!", "Error!")
                    return@addActionListener
                }

                if (detectString(username, "username")) {
                    return@addActionListener
                } else if (detectString(password, "password")) {
                    return@addActionListener
                }

                if (activeCode.isEmptyOrBlank()) {
                    showDialog("activation code can not be empty", "Error!")
                    return@addActionListener
                } else if (activeCode.isInvalid()) {
                    showDialog("invalid activation code contains an invalid symbol", "Error!")
                    return@addActionListener
                }

                loginMenu.state.text = "Registering account..."

                outputF.writeUTF("[REGIS]$username:$password:$HWID:$activeCode")

                when (inputF.readUTF()) {
                    "[ALREADY_HAVE_DIS_USERNAME]" -> {
                        showDialog("This username has been used. Please try other user names", "Error!")
                        loginMenu.state.text = "Use other username please"

                    }
                    "[INCORRECT_ACTIVECODE]" -> {
                        loginMenu.state.text = "RegisFailed :("
                        showDialog("Incorrect active code, ${chance--} chances left", "Error!")
                    }
                    "[REGIS_DONE]" -> {
                        showDialog("Your account has been activated. :)", "Configuration!")
                    }
                }
            }
        }
    }

    inline fun detectString(s: String?, name: String): Boolean {
        return when {
            s!!.isEmptyOrBlank() -> {
                showDialog("$name cant not be empty", "Error!")
                true
            }
            s.isInvalid() -> {
                showDialog("$name contains an invalid symbol", "Error!")
                true
            }
            s.tooLong() -> {
                showDialog("$name too long please let your $name under 100 character", "Error!")
                true
            }
            else -> false
        }
    }

}