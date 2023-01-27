package club.eridani.epsilon.client.mixin.mixins.render;

import club.eridani.epsilon.client.module.player.AntiAim;
import club.eridani.epsilon.client.util.Wrapper;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public class MixinModelBiped {

    @Shadow
    public ModelRenderer bipedRightArm;

    public int heldItemRight;

    @Shadow
    public ModelRenderer bipedHead;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        if (heldItemRight == 3) this.bipedRightArm.rotateAngleY = 0F;
        if (p_setRotationAngles_7_ instanceof EntityPlayer
                && p_setRotationAngles_7_.equals(Wrapper.getMc().player)
                && AntiAim.INSTANCE.isEnabled() && !AntiAim.INSTANCE.getServerSide()
                && AntiAim.INSTANCE.getFakePitch() != Wrapper.getMc().player.rotationPitch) {
            bipedHead.rotateAngleX = AntiAim.INSTANCE.getFakePitch() / (180F / (float) Math.PI);
        }
    }
}