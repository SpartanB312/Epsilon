package club.eridani.epsilon.client.command

import club.eridani.epsilon.client.util.text.ChatUtil

@DslMarker
annotation class CommandScope

class ExecutionScope(val args: Array<String>, val command: Command, val index: Int = 0)

@CommandScope
inline fun ExecutionScope.match(
    check: String,
    ignoreCase: Boolean = true,
    ignoreException: Boolean = true,
    block: ExecutionScope.() -> Unit
): Boolean = run(ignoreException, block) { it.equals(check, ignoreCase) }

@CommandScope
inline fun ExecutionScope.run(
    ignoreException: Boolean,
    block: ExecutionScope.() -> Unit,
    predicate: (String) -> Boolean = { true }
): Boolean = args.getOrNull(index)?.let {
    if (predicate(it)) {
        try {
            ExecutionScope(args, command, index + 1).block()
            true
        } catch (exception: Exception) {
            if (!ignoreException) exception.printStackTrace()
            ChatUtil.printErrorChatMessage("Error while executing command ${command.name}.Syntax : ${command.syntax}")
            false
        }
    } else false
} ?: false

@CommandScope
inline val ExecutionScope.value
    get() = this.args.getOrNull(index)

@CommandScope
inline fun ExecutionScope.execute(block: ExecutionScope.(String) -> Unit) = value?.let {
    ExecutionScope(args, command, index + 1).block(it)
}