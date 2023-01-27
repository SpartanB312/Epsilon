package club.eridani.epsilon.client.util.threads

import kotlinx.coroutines.*

@OptIn(ObsoleteCoroutinesApi::class)
val mainScope = CoroutineScope(newSingleThreadContext("Main Thread"))

val defaultScope = CoroutineScope(Dispatchers.Default)

val Job?.isActiveOrFalse get() = this?.isActive ?: false