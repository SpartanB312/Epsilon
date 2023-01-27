package club.eridani.epsilon.loader.verify

class User(var username: String, var password: String, private val HWID: String) {
    fun isValid() = username == "" && password == "" && HWID == ""
}