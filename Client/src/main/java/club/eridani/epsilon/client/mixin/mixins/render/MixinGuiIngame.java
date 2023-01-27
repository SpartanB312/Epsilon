package club.eridani.epsilon.client.mixin.mixins.render;

import club.eridani.epsilon.client.module.render.Crosshair;
import club.eridani.epsilon.client.util.Utils;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Inject(method = "renderAttackIndicator", at = @At(value = "HEAD"), cancellable = true)
    private void injectCrosshair(CallbackInfo ci) {
        if (!Utils.INSTANCE.nullCheck())
        if (Crosshair.INSTANCE.isEnabled() && Crosshair.INSTANCE.getCorsshair())
            ci.cancel();
    }
}