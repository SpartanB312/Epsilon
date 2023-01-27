package club.eridani.epsilon.client.common.collections

import java.util.*

@Suppress("NOTHING_TO_INLINE")
inline fun <E: Any> MutableCollection<E>.add(e: E?) {
    if (e != null) this.add(e)
}

/**
 * Returns the sum of all values produced by [selector] function applied to each element in the collection.
 */
inline fun <T> Iterable<T>.sumOfFloat(selector: (T) -> Float): Float {
    var sum = 0.0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

inline fun CharSequence.sumOfFloat(selector: (Char) -> Float): Float {
    var sum = 0.0f

    for (element in this) {
        sum += selector(element)
    }

    return sum
}


fun <K, V> NavigableMap<K, V>.lastValueOrNull(): V? =
    this.lastEntry()?.value

fun <K, V> NavigableMap<K, V>.firstValueOrNull(): V? =
    this.firstEntryOrNull()?.value

fun <K, V> NavigableMap<K, V>.firstEntryOrNull(): MutableMap.MutableEntry<K, V>? =
    firstEntry()

fun <K, V> NavigableMap<K, V>.lastEntryOrNull(): MutableMap.MutableEntry<K, V>? =
    lastEntry()

fun <K, V> MutableMap<K, V>.synchronized(): MutableMap<K, V> =
    Collections.synchronizedMap(this)

fun <K, V> SortedMap<K, V>.synchronized(): SortedMap<K, V> =
    Collections.synchronizedSortedMap(this)

fun <K, V> NavigableMap<K, V>.synchronized(): NavigableMap<K, V> =
    Collections.synchronizedNavigableMap(this)

fun <K, V> SortedMap<K, V>.firstKeyOrNull(): K? =
    try {
        firstKey()
    } catch (e: NoSuchElementException) {
        null
    }

@Suppress("NOTHING_TO_INLINE")
inline fun <E> MutableCollection<E>.synchronized(): MutableCollection<E> =
    Collections.synchronizedCollection(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <E> MutableList<E>.synchronized(): MutableList<E> =
    Collections.synchronizedList(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <E> MutableSet<E>.synchronized(): MutableSet<E> =
    Collections.synchronizedSet(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <E> SortedSet<E>.synchronized(): SortedSet<E> =
    Collections.synchronizedSortedSet(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <E> NavigableSet<E>.synchronized(): NavigableSet<E> =
    Collections.synchronizedNavigableSet(this)