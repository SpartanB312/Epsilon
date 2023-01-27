package club.eridani.epsilon.client.mixin.mixins;

import club.eridani.epsilon.client.event.EventBus;
import club.eridani.epsilon.client.event.events.ArrowVelocityEvent;
import net.minecraft.item.ItemBow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBow.class)
public class MixinItemBow {

    @Inject(method = "getArrowVelocity", at = @At("HEAD"), cancellable = true)
    private static void getArrowVelocity(int charge, CallbackInfoReturnable<Float> cir) {

        ArrowVelocityEvent event = new ArrowVelocityEvent(charge);
        EventBus.post(event);

        if (event.getCancelled()) {
            cir.cancel();
            cir.setReturnValue(event.getVelocity());
        }
    }


}
