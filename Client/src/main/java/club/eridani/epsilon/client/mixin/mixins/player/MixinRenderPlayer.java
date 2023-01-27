package club.eridani.epsilon.client.mixin.mixins.player;

import club.eridani.epsilon.client.module.render.ESP2D;
import club.eridani.epsilon.client.module.render.Nametags;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        ESP2D esp2D = ESP2D.INSTANCE;
        if ((esp2D.isEnabled() && esp2D.getDisplayName().getValue()) || Nametags.INSTANCE.isEnabled()) {
            info.cancel();
        }
    }

}