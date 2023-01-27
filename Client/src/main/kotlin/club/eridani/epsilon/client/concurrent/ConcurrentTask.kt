package club.eridani.epsilon.client.concurrent

import club.eridani.epsilon.client.util.Logger
import club.eridani.epsilon.client.util.threads.BackgroundJob
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.LongSupplier

/**
 * Meme codes by B_312
 */
@OptIn(ObsoleteCoroutinesApi::class)
object BackgroundScope : CoroutineScope by CoroutineScope(
    newFixedThreadPoolContext(
        Runtime.getRuntime().availableProcessors(),
        "Background scope"
    )
){
    private val jobs = LinkedHashMap<BackgroundJob, Job?>()
    private var started = false

    fun start() {
        started = true
        for ((job, _) in jobs) {
            jobs[job] = startJob(job)
        }
    }

    fun launchLooping(name: String, delay: Long, block: suspend CoroutineScope.() -> Unit): BackgroundJob {
        return launchLooping(BackgroundJob(name, delay, block))
    }

    fun launchLooping(job: BackgroundJob): BackgroundJob {
        if (!started) {
            jobs[job] = null
        } else {
            jobs[job] = startJob(job)
        }

        return job
    }

    fun cancel(job: BackgroundJob) = jobs.remove(job)?.cancel()

    private fun startJob(job: BackgroundJob): Job {
        return launch {
            while (isActive) {
                try {
                    job.block(this)
                } catch (e: Exception) {
                    Logger.warn("Error occurred while running background job ${job.name}")
                    e.printStackTrace()
                }
                delay(job.delay)
            }
        }
    }
}

fun runParallel(tasks: List<() -> Unit>) {
    runBlocking {
        tasks.forEach {
            launch(Dispatchers.Default) {
                it.invoke()
            }
        }
    }
}

fun runBackground(task: () -> Unit) {
    BackgroundScope.launch {
        task.invoke()
    }
}

fun runDelay(delayMs: Long, task: () -> Unit) {
    BackgroundScope.launch {
        delay(delayMs)
        task.invoke()
    }
}

fun runRepeat(delayMs: Long = 1000L, task: () -> Unit, suspended: Boolean = false): RepeatUnit {
    return RepeatUnit(
        delayMs = delayMs,
        task = task,
        scope = BackgroundScope,
        suspended = AtomicBoolean(suspended)
    )
}

fun runRepeat(delaySupplier: LongSupplier, task: () -> Unit, suspended: Boolean = false): RepeatUnit {
    return RepeatUnit(
        delaySupplier = delaySupplier,
        task = task,
        scope = BackgroundScope,
        suspended = AtomicBoolean(suspended)
    )
}

fun <T : Any> T.letDesync(task: (T) -> Unit) {
    BackgroundScope.launch {
        task.invoke(this@letDesync)
    }
}
