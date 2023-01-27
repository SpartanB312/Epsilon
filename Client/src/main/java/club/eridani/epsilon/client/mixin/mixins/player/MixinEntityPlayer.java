package club.eridani.epsilon.client.mixin.mixins.player;

import club.eridani.epsilon.client.event.EventBus;
import club.eridani.epsilon.client.event.events.PlayerPushEvent;
import club.eridani.epsilon.client.module.player.Hitbox;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    public MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    private void onIsPushedByWaterPre(CallbackInfoReturnable<Boolean> ciR) {
        PlayerPushEvent event = new PlayerPushEvent(PlayerPushEvent.Type.LIQUID);
        EventBus.post(event);
        if (event.getCancelled()) ciR.setReturnValue(false);
    }

    public float getCollisionBorderSize() {
        return Hitbox.INSTANCE.isEnabled() ? Hitbox.getSize() : super.getCollisionBorderSize();
    }

}
