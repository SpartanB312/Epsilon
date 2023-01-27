package club.eridani.epsilon.client.menu.alt

class AltAccount(var username: String, var passwords: String?) {
    val isCracked: Boolean
        get() = passwords == null || passwords!!.isEmpty()
}