package club.eridani.epsilon.client.mixin.mixins.render;

import club.eridani.epsilon.client.module.player.Reach;
import club.eridani.epsilon.client.module.render.AntiOverlay;
import club.eridani.epsilon.client.module.render.CameraClip;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = Integer.MAX_VALUE)
public class MixinEntityRenderer {

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;clear(I)V", ordinal = 1, shift = At.Shift.AFTER))
    private void onRenderWorldPassClear(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        /*
        Render3DDecentralizedEvent.INSTANCE.post(new Render3DDecentralizedEvent.Render3DEventData(partialTicks));
        EventBus.post(new Render3DEvent(partialTicks));
         */
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;distanceTo(Lnet/minecraft/util/math/Vec3d;)D", ordinal = 2))
    private double distanceTo(Vec3d vec3d, Vec3d p_72438_1_) {
        return vec3d.distanceTo(p_72438_1_) - (Reach.INSTANCE.isEnabled() ? Reach.INSTANCE.getReachAdd().getValue() : 0);
    }


    @ModifyVariable(method = "orientCamera", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    public RayTraceResult orientCameraStoreRayTraceBlocks(RayTraceResult value) {
        if (CameraClip.INSTANCE.isEnabled()) {
            return null;
        } else {
            return value;
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtCam(float partialTicks, CallbackInfo ci) {
        if (AntiOverlay.INSTANCE.isEnabled() && AntiOverlay.INSTANCE.getHurtCam()) ci.cancel();
    }

}
