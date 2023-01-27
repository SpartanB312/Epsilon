package club.eridani.epsilon.client.mixin.mixins.world;

import club.eridani.epsilon.client.module.render.WallHack;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    public void shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> info) {
        if (WallHack.INSTANCE.isEnabled()) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "getRenderLayer", at = @At("HEAD"), cancellable = true)
    public void getRenderLayer(CallbackInfoReturnable<BlockRenderLayer> info) {
        if (WallHack.INSTANCE.isEnabled()) {
            if (!WallHack.INSTANCE.getBlocks().contains((Block) (Object) this)) {
                info.setReturnValue(BlockRenderLayer.TRANSLUCENT);
            }
        }
    }

    @Inject(method = "getLightValue", at = @At("HEAD"), cancellable = true)
    public void getLightValue(CallbackInfoReturnable<Integer> info) {
        if (WallHack.INSTANCE.isEnabled()) {
            info.setReturnValue(WallHack.INSTANCE.getLight().getValue());
        }
    }

}
