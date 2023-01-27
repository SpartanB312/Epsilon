package club.eridani.epsilon.client.util

open class Timer {

    var time = currentTime; protected set

    protected val currentTime get() = System.currentTimeMillis()

    fun passed(time: Int): Boolean {
        return currentTime - time >= this.time
    }

    fun passedAndReset(time: Int): Boolean {
        val passed = passed(time)
        if (passed) reset()
        return passed
    }

    fun reset() {
        time = currentTime
    }

    fun reset(offset: Long = 0L) {
        time = currentTime + offset
    }

    fun reset(offset: Int = 0) {
        time = currentTime + offset
    }
}

class TickTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun tick(delay: Int, resetIfTick: Boolean = true): Boolean {
        return tick(delay.toLong(), resetIfTick)
    }

    fun tick(delay: Long, resetIfTick: Boolean = true): Boolean {
        return if (currentTime - time >= delay * timeUnit.multiplier) {
            if (resetIfTick) time = currentTime
            true
        } else {
            false
        }
    }

    fun tick(delay: Int): Boolean {
        val current = System.currentTimeMillis()
        return current - time >= delay * timeUnit.multiplier
    }

    fun tick(delay: Long): Boolean {
        val current = System.currentTimeMillis()
        return current - time >= delay * timeUnit.multiplier
    }

    fun tick(delay: Int, unit: TimeUnit): Boolean {
        val current = System.currentTimeMillis()
        return current - time >= delay * unit.multiplier
    }

    fun tick(delay: Long, unit: TimeUnit): Boolean {
        val current = System.currentTimeMillis()
        return current - time >= delay * unit.multiplier
    }

    fun tickAndReset(delay: Int): Boolean {
        val current = System.currentTimeMillis()
        return if (current - time >= delay * timeUnit.multiplier) {
            time = current
            true
        } else {
            false
        }
    }

    fun tickAndReset(delay: Long): Boolean {
        val current = System.currentTimeMillis()
        return if (current - time >= delay * timeUnit.multiplier) {
            time = current
            true
        } else {
            false
        }
    }

    fun tickAndReset(delay: Int, unit: TimeUnit): Boolean {
        val current = System.currentTimeMillis()
        return if (current - time >= delay * unit.multiplier) {
            time = current
            true
        } else {
            false
        }
    }

    fun tickAndReset(delay: Long, unit: TimeUnit): Boolean {
        val current = System.currentTimeMillis()
        return if (current - time >= delay * unit.multiplier) {
            time = current
            true
        } else {
            false
        }
    }

}

class StopTimer(val timeUnit: TimeUnit = TimeUnit.MILLISECONDS) : Timer() {
    fun stop(): Long {
        return (currentTime - time) / timeUnit.multiplier
    }
}

enum class TimeUnit(val multiplier: Long) {
    MILLISECONDS(1L),
    TICKS(50L),
    SECONDS(1000L),
    MINUTES(60000L);
}