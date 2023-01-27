package club.eridani.epsilon.client.util.graphics.render

import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.SpartanCore.addAsyncUpdateListener
import club.eridani.epsilon.client.util.Wrapper.mc
import club.eridani.epsilon.client.util.onRender2D

@Suppress("NOTHING_TO_INLINE")
open class AsyncRenderer(private val onUpdate: AsyncRenderer.() -> Unit) : RawAsyncRenderer() {

    override fun update() {
        tempTasks.clear()
        onUpdate.invoke(this)
        synchronized(tasks) {
            tasks.clear()
            tasks.addAll(tempTasks)
        }
    }

}

fun AbstractModule.asyncRender(
    noRender2D: Boolean = this is HUDModule,
    onUpdate: AsyncRenderer.() -> Unit
): AsyncRenderer =
    asyncRendererOf(onUpdate).also { renderer ->
        onAsyncUpdate {
            if (mc.player == null || mc.world == null) return@onAsyncUpdate
            renderer.update()
        }
        if (!noRender2D) onRender2D {
            renderer.render()
        }
    }

fun Any.asyncRender(
    noRender2D: Boolean = this is HUDModule,
    onUpdate: AsyncRenderer.() -> Unit
): AsyncRenderer =
    asyncRendererOf(onUpdate).also { renderer ->
        addAsyncUpdateListener {
            if (mc.player == null || mc.world == null) return@addAsyncUpdateListener
            renderer.update()
        }
        if (!noRender2D) onRender2D {
            renderer.render()
        }
    }

fun asyncRendererOf(onUpdate: AsyncRenderer.() -> Unit): AsyncRenderer = AsyncRenderer(onUpdate)