package club.eridani.epsilon.client.event

open class Event {
    open var stage: Stage = Stage.PRE

    open fun post() {
        EventBus.post(this)
    }
}

abstract class ProfilerEvent(val profilerName: String) : Event() {
    override fun post() {
        EventBus.postProfiler(this)
    }
}

private interface ICancellable {
    var cancelled: Boolean

    fun cancel() {
        cancelled = true
    }
}

open class Cancellable : ICancellable, Event() {
    override var cancelled = false
        set(value) {
            field = field || value
        }
}

enum class Stage(val displayName: String) {
    PRE("Pre"),
    PERI("Peri"),
    POST("Post")
}