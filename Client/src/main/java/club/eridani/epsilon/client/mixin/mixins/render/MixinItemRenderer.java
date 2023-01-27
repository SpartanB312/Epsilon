package club.eridani.epsilon.client.mixin.mixins.render;

import club.eridani.epsilon.client.event.decentralized.events.client.RenderItemAnimationDecentralizedEvent;
import club.eridani.epsilon.client.event.decentralized.events.render.RenderOverlayDecentralizedEvent;
import club.eridani.epsilon.client.module.render.ViewModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(
            method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V")
    )
    private void onRenderItemTransformAnimationPre(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
        RenderItemAnimationDecentralizedEvent.RenderItemAnimationData data = new RenderItemAnimationDecentralizedEvent.RenderItemAnimationData(stack, hand, p_187457_5_, RenderItemAnimationDecentralizedEvent.Transform.INSTANCE);
        RenderItemAnimationDecentralizedEvent.Transform.INSTANCE.post(data);
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    public void renderItemInFirstPersonHook(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
        RenderItemAnimationDecentralizedEvent.RenderItemAnimationData data = new RenderItemAnimationDecentralizedEvent.RenderItemAnimationData(stack, hand, p_187457_5_, RenderItemAnimationDecentralizedEvent.Render.INSTANCE);
        RenderItemAnimationDecentralizedEvent.Render.INSTANCE.post(data);

        if (data.getCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "renderWaterOverlayTexture", at = @At("HEAD"), cancellable = true)
    public void onPreRenderWaterOverlayTexture(float partialTicks, CallbackInfo ci) {
        final RenderOverlayDecentralizedEvent.RenderOverlayData data = new RenderOverlayDecentralizedEvent.RenderOverlayData(RenderOverlayDecentralizedEvent.OverlayType.WATER);
        RenderOverlayDecentralizedEvent.INSTANCE.post(data);
        if (data.getCancelled()) ci.cancel();
    }

    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onPreRenderFireInFirstPerson(CallbackInfo ci) {
        final RenderOverlayDecentralizedEvent.RenderOverlayData data = new RenderOverlayDecentralizedEvent.RenderOverlayData(RenderOverlayDecentralizedEvent.OverlayType.FIRE);
        RenderOverlayDecentralizedEvent.INSTANCE.post(data);
        if (data.getCancelled()) ci.cancel();
    }

    @Inject(method = "renderSuffocationOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderSuffocationOverlayPre(TextureAtlasSprite sprite, CallbackInfo ci) {
        final RenderOverlayDecentralizedEvent.RenderOverlayData data = new RenderOverlayDecentralizedEvent.RenderOverlayData(RenderOverlayDecentralizedEvent.OverlayType.BLOCK);
        RenderOverlayDecentralizedEvent.INSTANCE.post(data);
        if (data.getCancelled()) ci.cancel();
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    private void transformSideFirstPersonInvokePushMatrix(AbstractClientPlayer player, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo ci) {
        ViewModel.translate(stack, hand, player);
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    private void transformSideFirstPersonInvokeRenderItemSide(AbstractClientPlayer player, float partialTicks, float pitch, EnumHand hand, float swingProgress, ItemStack stack, float equippedProgress, CallbackInfo ci) {
        ViewModel.rotateAndScale(stack, hand, player);
    }
}
