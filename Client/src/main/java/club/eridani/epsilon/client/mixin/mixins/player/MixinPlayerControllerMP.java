package club.eridani.epsilon.client.mixin.mixins.player;

import club.eridani.epsilon.client.event.events.BlockEvent;
import club.eridani.epsilon.client.event.events.InteractEvent;
import club.eridani.epsilon.client.event.events.PlayerAttackEvent;
import club.eridani.epsilon.client.module.player.Reach;
import club.eridani.epsilon.client.util.Wrapper;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {
    @Shadow
    private GameType currentGameType;
    @Shadow @Final
    private Minecraft mc;
    @Shadow private float curBlockDamageMP;
    @Shadow private boolean isHittingBlock;
    @Shadow private BlockPos currentBlock;
    @Shadow private ItemStack currentItemHittingBlock;
    @Shadow private float stepSoundTickCounter;
    @Shadow @Final private NetHandlerPlayClient connection;

    @Shadow
    public abstract boolean onPlayerDestroyBlock(BlockPos pos);

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity, CallbackInfo ci) {
        if (targetEntity == null) return;
        PlayerAttackEvent event = new PlayerAttackEvent(targetEntity);
        event.post();

        if (event.getCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "clickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;", ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void clickBlock$Inject$INVOKE$getBlockState(BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir, PlayerInteractEvent.LeftClickBlock forgeEvent) {
        InteractEvent.Block.LeftClick event = new InteractEvent.Block.LeftClick(pos, side);
        event.post();

        if (event.getCancelled()) {
            IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(pos);
            Minecraft.getMinecraft().getTutorial().onHitBlock(Minecraft.getMinecraft().world, pos, blockState, 0.0F);
            boolean flag = blockState.getMaterial() != Material.AIR;

            if (flag && this.curBlockDamageMP == 0.0F) {
                if (forgeEvent.getUseBlock() != Event.Result.DENY) {
                    blockState.getBlock().onBlockClicked(Minecraft.getMinecraft().world, pos, Minecraft.getMinecraft().player);
                }
            }

            if (forgeEvent.getUseItem() == Event.Result.DENY) {
                cir.setReturnValue(true);
                return;
            }

            if (flag && blockState.getPlayerRelativeBlockHardness(Minecraft.getMinecraft().player, Minecraft.getMinecraft().player.world, pos) >= 1.0F) {
                this.onPlayerDestroyBlock(pos);
            } else {
                this.isHittingBlock = true;
                this.currentBlock = pos;
                this.currentItemHittingBlock = Minecraft.getMinecraft().player.getHeldItemMainhand();
                this.curBlockDamageMP = 0.0F;
                this.stepSoundTickCounter = 0.0F;
                Minecraft.getMinecraft().world.sendBlockBreakProgress(Minecraft.getMinecraft().player.getEntityId(), this.currentBlock, (int) (this.curBlockDamageMP * 10.0F) - 1);
            }

            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBlockReachDistance", at = @At("HEAD"), cancellable = true)
    public void onGetBlockReachDistancePre(CallbackInfoReturnable<Float> cir) {
        if (Reach.INSTANCE.isEnabled()) {
            float attrib = (float) Wrapper.getMc().player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()
                    + (Reach.INSTANCE.isEnabled() ? Reach.INSTANCE.getReachAdd().getValue() : 0);
            cir.setReturnValue(this.currentGameType.isCreative() ? attrib : attrib - 0.5F);
        }
    }

    @Inject(method = "clickBlock", at = @At(value = "HEAD"), cancellable = true)
    private void clickBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        BlockEvent.Click event = new BlockEvent.Click(pos, face);
        event.post();
        if (event.getCancelled()) {
            info.cancel();
        }
    }

    @Inject(method = "processRightClickBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onProcessRightClickBlockPre(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        BlockEvent.Place event = new BlockEvent.Place(pos, direction, vec, hand);
        event.post();
        if (event.getCancelled()) {
            cir.setReturnValue(EnumActionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At(value = "HEAD"), cancellable = true)
    private void onPlayerDamageBlockHook(BlockPos pos, EnumFacing face, CallbackInfoReturnable<Boolean> info) {
        BlockEvent.Damage event = new BlockEvent.Damage(pos, face);
        event.post();
        if (event.getCancelled()) {
            info.cancel();
        }
    }

}
