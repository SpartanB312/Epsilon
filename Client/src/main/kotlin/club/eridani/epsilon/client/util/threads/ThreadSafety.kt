package club.eridani.epsilon.client.util.threads

import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.util.Wrapper
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
suspend fun <R> runSafeSuspend(block: suspend SafeClientEvent.() -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return SafeClientEvent.instance?.block()
}

@OptIn(ExperimentalContracts::class)
fun <R> runSafe(block: SafeClientEvent.() -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (Wrapper.player == null || Wrapper.world == null) null
    else SafeClientEvent.instance?.block()
}

@OptIn(ExperimentalContracts::class)
inline fun runSafeOrFalse(block: SafeClientEvent.() -> Boolean): Boolean {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val instance = SafeClientEvent.instance
    return if (instance != null) {
        block.invoke(instance)
    } else {
        false
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T : Any, R> T.runSynchronized(block: T.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return synchronized(this) {
        block.invoke(this)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> T.runIf(boolean: Boolean, block: T.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return if (boolean) block.invoke(this) else this
}
