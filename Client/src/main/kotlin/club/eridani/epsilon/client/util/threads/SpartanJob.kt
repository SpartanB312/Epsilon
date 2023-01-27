package club.eridani.epsilon.client.util.threads

class SpartanJob(private val task: () -> Unit) {

    var isFinished = false

    fun execute() {
        task.invoke()
        isFinished = true
    }

}