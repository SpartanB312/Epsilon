package club.eridani.epsilon.client.mixin.mixins.entity;

import club.eridani.epsilon.client.event.EventBus;
import club.eridani.epsilon.client.event.events.EntityCollisionEvent;
import club.eridani.epsilon.client.module.movement.SafeWalk;
import club.eridani.epsilon.client.module.movement.Scaffold;
import club.eridani.epsilon.client.module.player.Hitbox;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {


    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocity(Entity entity, double x, double y, double z) {

        EntityCollisionEvent entityCollisionEvent = new EntityCollisionEvent(entity, x, y, z);

        EventBus.post(entityCollisionEvent);

        if (entityCollisionEvent.getCancelled()) return;

        entity.motionX += x;
        entity.motionY += y;
        entity.motionZ += z;

        entity.isAirBorne = true;
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(Entity entity) {
        boolean sneakInput = entity.isSneaking();

        return SafeWalk.INSTANCE.getShouldSneak() || (Scaffold.INSTANCE.getSafeWalk() && Scaffold.INSTANCE.isEnabled()) || sneakInput;
    }

    @Inject(
            method = "getCollisionBorderSize",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getCollisionBorderSize(CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (Hitbox.INSTANCE.isEnabled()) {
            double hitBox = Hitbox.getSize();
            callbackInfoReturnable.setReturnValue((float) hitBox);
            callbackInfoReturnable.cancel();
        }
    }

}
