package club.eridani.epsilon.client.event

import club.eridani.epsilon.client.common.interfaces.Helper
import io.netty.util.internal.ConcurrentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("NOTHING_TO_INLINE")
object EventBus : Helper {

    val registered = ConcurrentHashMap<Any, CopyOnWriteArrayList<Listener<*>>>()
    val registeredParallel = ConcurrentHashMap<Any, CopyOnWriteArrayList<ParallelListener<*>>>()

    val subscribed = ConcurrentHashMap<Class<out Any>, ConcurrentSkipListSet<Listener<Any>>>()
    val subscribedParallel = ConcurrentHashMap<Class<out Any>, ConcurrentSet<ParallelListener<Any>>>()

    inline fun <T : Listener<*>> register(owner: Any, listener: T) {
        registered.getOrPut(owner, ::CopyOnWriteArrayList).add(listener)
    }

    inline fun <T : ParallelListener<*>> register(owner: Any, listener: T) {
        registeredParallel.getOrPut(owner, ::CopyOnWriteArrayList).add(listener)
    }


    @JvmStatic
    inline fun subscribe(obj: Any) {
        registered[obj]?.forEach(EventBus::subscribe)
        registeredParallel[obj]?.forEach(EventBus::subscribe)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun subscribe(listener: Listener<*>) {
        subscribed.getOrPut(listener.eventClass, ::ConcurrentSkipListSet).add(listener as Listener<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun subscribe(listener: ParallelListener<*>) {
        subscribedParallel.getOrPut(listener.eventClass, ::ConcurrentSet).add(listener as ParallelListener<Any>)
    }

    @JvmStatic
    inline fun unsubscribe(obj: Any) {
        registered[obj]?.forEach(EventBus::unsubscribe)
        registeredParallel[obj]?.forEach(EventBus::unsubscribe)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun unsubscribe(listener: Listener<*>) {
        subscribed[listener.eventClass]?.remove(listener as Listener<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    inline fun unsubscribe(listener: ParallelListener<*>) {
        subscribedParallel[listener.eventClass]?.remove(listener as ParallelListener<Any>)
    }

    @JvmStatic
    inline fun postProfiler(event: ProfilerEvent) {
        mc.profiler.startSection(event.profilerName)
        postProfiler0(event)
        mc.profiler.endSection()
    }

    inline fun postProfiler0(event: Any) {
        mc.profiler.startSection("serial/concurrent")
        subscribed[event.javaClass]?.forEach {
            mc.profiler.startSection(it.ownerName)
            it.function.accept(event)
            mc.profiler.endSection()
        }

        mc.profiler.endStartSection("parallel")
        invokeParallel(event)
        mc.profiler.endSection()
    }

    @JvmStatic
    inline fun post(event: Any) {
        subscribed[event.javaClass]?.forEach {
            it.function.accept(event)
        }

        invokeParallel(event)
    }

    inline fun invokeParallel(event: Any) {
        val listeners = subscribedParallel[event.javaClass]
        if (!listeners.isNullOrEmpty()) {
            runBlocking {
                listeners.forEach {
                    launch(Dispatchers.Default) {
                        it.function.invoke(event)
                    }
                }
            }
        }
    }
}