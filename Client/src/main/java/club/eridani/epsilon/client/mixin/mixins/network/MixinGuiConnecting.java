package club.eridani.epsilon.client.mixin.mixins.network;

import club.eridani.epsilon.client.event.SafeClientEvent;
import club.eridani.epsilon.client.event.events.ConnectionEvent;
import net.minecraft.client.multiplayer.GuiConnecting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public class MixinGuiConnecting {

    @Inject(method = "connect", at = @At("HEAD"))
    private void onPreConnect(CallbackInfo info) {
        SafeClientEvent.Companion.update();
        ConnectionEvent.Connect.INSTANCE.post();
    }


}
