package club.eridani.epsilon.client.command

abstract class Command(
    val name: String,
    val prefix: String,
    val description: String,
    val syntax: String,
    val block: ExecutionScope.() -> Unit
)