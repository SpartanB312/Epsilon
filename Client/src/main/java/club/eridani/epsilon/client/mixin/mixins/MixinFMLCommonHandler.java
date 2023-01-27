package club.eridani.epsilon.client.mixin.mixins;

import club.eridani.epsilon.client.management.InputManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FMLCommonHandler.class)
public class MixinFMLCommonHandler {

    @Inject(method = "fireKeyInput", at = @At("HEAD"), remap = false)
    public void onKey(CallbackInfo ci) {
        InputManager.onKeyInput();
    }

    @Inject(method = "fireMouseInput", at = @At("HEAD"), remap = false)
    public void onMouse(CallbackInfo ci) {
        InputManager.onMouseInput();
    }

}
