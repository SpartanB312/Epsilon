package club.eridani.epsilon.client.mixin.mixins.render;

import club.eridani.epsilon.client.event.events.RenderEntityEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderGlobal.class)
public class MixinRenderGlobal {

//    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
//    public void onDrawSelectionBoxPre(EntityPlayer player, RayTraceResult result, int execute, float partialTicks, CallbackInfo ci) {
//        BlockHighlightEvent e = new BlockHighlightEvent(player, result, execute, partialTicks);
//        EridaniCore.EVENT_BUS.post(e);
//        if (e.isCanceled()) ci.cancel();
//    }

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void renderEntitiesHead(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(true);
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    public void renderEntitiesReturn(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(false);
    }
}
