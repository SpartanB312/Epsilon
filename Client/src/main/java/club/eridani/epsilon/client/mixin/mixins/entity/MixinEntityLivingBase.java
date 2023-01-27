package club.eridani.epsilon.client.mixin.mixins.entity;

import club.eridani.epsilon.client.management.PlayerPacketManager;
import club.eridani.epsilon.client.module.player.AntiAim;
import club.eridani.epsilon.client.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

    @Shadow
    public float swingProgress;

    @Shadow public float renderYawOffset;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @SuppressWarnings("ConstantConditions")
    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;updateDistance(FF)F"), index = 0)
    public float onUpdate$ModifyArg$INVOKE$updateDistance$1(float value) {
        if ((Object) this == Wrapper.getPlayer()) {
            float serverYaw = PlayerPacketManager.INSTANCE.getRotation().getX();
            if (!AntiAim.INSTANCE.getServerSide()) {
                serverYaw = AntiAim.INSTANCE.getFakeYaw();
            }
            
            double x = this.posX - this.prevPosX;
            double z = this.posZ - this.prevPosZ;
            float f3 = (float) (x * x + z * z);

            if (this.swingProgress > 0.0f) {
                value = serverYaw;
            } else if (f3 > 0.0025000002f) {
                float f1 = (float) MathHelper.atan2(z, x) * (180.0f / (float) Math.PI) - 90.0f;
                float f2 = MathHelper.abs(MathHelper.wrapDegrees(serverYaw) - f1);

                if (95.0f < f2 && f2 < 265.0f) {
                    value = f1 - 180.0f;
                } else {
                    value = f1;
                }
            }
        }

        return value;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "updateDistance", at = @At("HEAD"), cancellable = true)
    public void updateDistance$Inject$HEAD(float yaw, float distance, CallbackInfoReturnable<Float> cir) {
        if ((Object) this == Wrapper.getPlayer()) {
            float serverYaw = PlayerPacketManager.INSTANCE.getRotation().getX();
            if (AntiAim.INSTANCE.isEnabled() && !AntiAim.INSTANCE.getServerSide()) {
                serverYaw = AntiAim.INSTANCE.getFakeYaw();
            }

            float f = MathHelper.wrapDegrees(yaw - this.renderYawOffset);
            this.renderYawOffset += f * 0.3F;
            float f1 = MathHelper.wrapDegrees(serverYaw - this.renderYawOffset);
            boolean flag = f1 < -90.0F || f1 >= 90.0F;

            if (f1 < -75.0F) {
                f1 = -75.0F;
            }

            if (f1 >= 75.0F) {
                f1 = 75.0F;
            }

            this.renderYawOffset = serverYaw - f1;

            if (f1 * f1 > 2500.0F) {
                this.renderYawOffset += f1 * 0.2F;
            }

            if (flag) {
                distance *= -1.0F;
            }

            cir.setReturnValue(distance);
        }
    }
}
