package club.eridani.epsilon.client.mixin.mixins.accessor.network;

import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayerTryUseItemOnBlock.class)
public interface AccessorCPacketPlayerTryUseItemOnBlock {
    @Accessor("placedBlockDirection")
    void epsilonSetSide(EnumFacing value);
}
