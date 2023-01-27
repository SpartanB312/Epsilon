package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.event.decentralized.events.network.PacketDecentralizedEvent
import club.eridani.epsilon.client.event.events.Render2DEvent
import club.eridani.epsilon.client.event.events.Render3DEvent
import club.eridani.epsilon.client.event.events.SpartanTick
import club.eridani.epsilon.client.event.events.TickEvent
import club.eridani.epsilon.client.event.listener
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11

inline fun Any.onPacketSend(crossinline action: (PacketDecentralizedEvent.PacketEventData) -> Unit) {
    listener<PacketDecentralizedEvent.PacketEventData> {
        if (it.father == PacketDecentralizedEvent.Send) action.invoke(it)
    }
}

inline fun Any.onPacketReceive(crossinline action: (PacketDecentralizedEvent.PacketEventData) -> Unit) {
    listener<PacketDecentralizedEvent.PacketEventData> {
        if (it.father == PacketDecentralizedEvent.Receive) action.invoke(it)
    }
}

inline fun Any.onPacketPostSend(crossinline action: (PacketDecentralizedEvent.PacketEventData) -> Unit) {
    listener<PacketDecentralizedEvent.PacketEventData> {
        if (it.father == PacketDecentralizedEvent.PostSend) action.invoke(it)
    }
}

inline fun Any.onPacketPostReceive(crossinline action: (PacketDecentralizedEvent.PacketEventData) -> Unit) {
    listener<PacketDecentralizedEvent.PacketEventData> {
        if (it.father == PacketDecentralizedEvent.PostReceive) action.invoke(it)
    }
}

inline fun Any.onTick(crossinline action: () -> Unit) {
    listener<TickEvent> {
        action.invoke()
    }
}

inline fun Any.onRender2D(crossinline action: (Render2DEvent) -> Unit) {
    listener<Render2DEvent> {
        action.invoke(it)
    }
}

inline fun Any.onSpartanTick(crossinline action: () -> Unit) {
    listener<SpartanTick> {
        action.invoke()
    }
}

inline fun Any.onRender3D(crossinline action: (Render3DEvent) -> Unit) {
    listener<Render3DEvent> {
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        GlStateManager.disableDepth()
        GlStateManager.glLineWidth(1f)

        action.invoke(it)

        GlStateManager.glLineWidth(1f)
        GlStateManager.shadeModel(GL11.GL_FLAT)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
    }
}