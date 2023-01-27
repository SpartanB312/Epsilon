package club.eridani.epsilon.client.util

import kotlin.reflect.KProperty

/**
 * Use this to creat a final field to lock
 */
class Lockable<T>(var value: T?){

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }

}

fun <T> create(): Lockable<T> {
    return Lockable(null)
}

