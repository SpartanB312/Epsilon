package club.eridani.epsilon.client.mixin.mixins.render;

import club.eridani.epsilon.client.event.EventBus;
import club.eridani.epsilon.client.event.events.RenderEntityEvent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = RenderManager.class, priority = 114514)
public class MixinRenderManager {
    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;setRenderOutlines(Z)V", shift = At.Shift.BEFORE), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderEntity$Inject$INVOKE$setRenderOutlines$BEFORE(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci, Render<Entity> render) {
        if (entity == null || render == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent eventAll = new RenderEntityEvent.All.Pre(entity, x, y, z, yaw, partialTicks, render);
        EventBus.post(eventAll);

        if (eventAll.getCancelled()) {
            ci.cancel();
        } else if (!(entity instanceof EntityLivingBase)) {
            RenderEntityEvent eventModel = RenderEntityEvent.Model.Pre.of(entity, x, y, z, yaw, partialTicks, render);
            EventBus.post(eventModel);
        }
    }

    @Inject(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderEntity$Inject$INVOKE$doRender$AFTER(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci, Render<Entity> render) {
        if (entity == null || render == null || !RenderEntityEvent.getRenderingEntities() || entity instanceof EntityLivingBase)
            return;

        RenderEntityEvent event = RenderEntityEvent.Model.Post.of(entity, x, y, z, yaw, partialTicks, render);
        EventBus.post(event);
    }

    @Inject(method = "renderEntity", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void renderEntity$Inject$RETURN(Entity entity, double x, double y, double z, float yaw, float partialTicks, boolean debug, CallbackInfo ci, Render<Entity> render) {
        if (entity == null || render == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent event = new RenderEntityEvent.All.Post(entity, x, y, z, yaw, partialTicks, render);
        EventBus.post(event);
    }
}
