package club.eridani.epsilon.client.event

import club.eridani.epsilon.client.ForgeRegister
import club.eridani.epsilon.client.event.decentralized.events.client.Render3DDecentralizedEvent
import club.eridani.epsilon.client.event.events.Render3DEvent
import club.eridani.epsilon.client.management.WorldManager
import club.eridani.epsilon.client.util.graphics.GlStateUtils
import club.eridani.epsilon.client.util.graphics.ProjectionUtils
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ForgeAccessor {

    fun subscribe() {
        ForgeRegister.forceRegister(ForgeAccessor)
    }

    @SubscribeEvent
    fun onRender3D(event: RenderWorldLastEvent) {
        ProjectionUtils.updateMatrix()
        RenderUtils3D.prepareGL()
        Render3DDecentralizedEvent.post(Render3DDecentralizedEvent.Render3DEventData(event.partialTicks))
        EventBus.post(Render3DEvent(event.partialTicks))
        RenderUtils3D.releaseGL()
        GlStateUtils.useProgramForce(0)
    }

    @SubscribeEvent
    fun onLoadWorld(event: WorldEvent.Load) {
        if (event.world.isRemote) {
            event.world.addEventListener(WorldManager)
            club.eridani.epsilon.client.event.events.WorldEvent.Load.post()
        }
    }

    @SubscribeEvent
    fun onUnloadWorld(event: WorldEvent.Unload) {
        if (event.world.isRemote) {
            event.world.removeEventListener(WorldManager)
            club.eridani.epsilon.client.event.events.WorldEvent.Unload.post()
        }
    }

}