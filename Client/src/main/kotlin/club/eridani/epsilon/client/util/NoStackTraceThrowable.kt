package club.eridani.epsilon.client.util

class NoStackTraceThrowable @JvmOverloads constructor(msg: String? = "SUCK MY DICK") : RuntimeException(msg) {
    override fun toString(): String {
        return "FUCK OFF NIGGA UR MUN GAY"
    }

    @Synchronized
    override fun fillInStackTrace(): Throwable {
        return this
    }

    init {
        stackTrace = arrayOfNulls(0)
    }
}