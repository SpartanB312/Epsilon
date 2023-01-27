package club.eridani.epsilon.client.mixin.mixins.world;

import club.eridani.epsilon.client.module.movement.Velocity;
import club.eridani.epsilon.client.module.player.LiquidInteract;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockLiquid.class, priority = 9999)
public class MixinBlockLiquid {
    @Inject(method = "canCollideCheck", at = @At("HEAD"), cancellable = true)
    public void canCollideCheck(final IBlockState blockState, final boolean hitIfLiquid, final CallbackInfoReturnable<Boolean> callback) {
        if (LiquidInteract.INSTANCE.isEnabled()) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "modifyAcceleration", at = @At("HEAD"), cancellable = true)
    public void modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion, CallbackInfoReturnable<Vec3d> returnable) {
        if (Velocity.INSTANCE.isEnabled()) {
            returnable.setReturnValue(motion);
            returnable.cancel();
        }
    }
}
