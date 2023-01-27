package club.eridani.epsilon.client.common.extensions

import club.eridani.epsilon.client.util.Wrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.io.File

inline val BlockPos.canBeClicked: Boolean
    get() = Wrapper.world?.getBlockState(this)?.block!!.canCollideCheck(Wrapper.world?.getBlockState(this), false)

inline val BlockPos.boundingBox: AxisAlignedBB
    get() {
        val iBlockState = Wrapper.mc.world.getBlockState(this)
        return iBlockState.getSelectedBoundingBox(Wrapper.mc.world, this)
            .grow(0.002)
            .offset(
                -Wrapper.mc.renderManager.renderPosX,
                -Wrapper.mc.renderManager.renderPosY,
                -Wrapper.mc.renderManager.renderPosZ
            )
    }

fun MutableList<Runnable>.addTask(runnable: Runnable) {
    this.add(runnable)
}

fun CoroutineScope.runAsyncSafeIO(ignoreException: Boolean = true, job: () -> Unit): Job {
    return this.launch {
        job.runSafeTask(ignoreException)
    }
}

fun IntArray.copy(): IntArray {
    return IntArray(this.size) { index ->
        this[index]
    }
}

inline fun <reified T : Any> Array<T>.copy(): Array<T> {
    return Array(this.size) { index ->
        this[index]
    }
}

fun File.isNotExist(): Boolean {
    return !this.exists()
}

inline fun runSafeTask(ignoreException: Boolean = false, function: () -> Unit): Boolean {
    return try {
        function.invoke()
        true
    } catch (exception: Exception) {
        if (!ignoreException) exception.printStackTrace()
        false
    }
}

fun (() -> Unit).runSafeTask(ignoreException: Boolean = true): Boolean {
    return try {
        this.invoke()
        true
    } catch (exception: Exception) {
        if (!ignoreException) exception.printStackTrace()
        false
    }
}

fun <T : Number> T.isPositive(): Boolean {
    return this.toDouble() >= 0
}

fun <T : Number> T.isNegative(): Boolean {
    return this.toDouble() < 0
}

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}

inline fun <T> MutableCollection<T>.addIf(obj: T, predicate: (MutableCollection<T>) -> Boolean) {
    if (predicate.invoke(this)) this.add(obj)
}

inline fun <T, U> MutableMap<T, U>.addIf(obj1: T, obj2: U, predicate: (MutableMap<T, U>) -> Boolean) {
    if (predicate.invoke(this)) this[obj1] = obj2
}