package club.eridani.epsilon.client.common.extensions

import club.eridani.epsilon.client.util.ItemUtil
import club.eridani.epsilon.client.util.Wrapper
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos

inline val IBlockState.isBlacklisted: Boolean
    get() = ItemUtil.blackList.contains(this.block)

inline val IBlockState.isLiquid: Boolean
    get() = this.material.isLiquid

inline val IBlockState.isWater: Boolean
    get() = this.block == Blocks.WATER

inline val IBlockState.isReplaceable: Boolean
    get() = this.material.isReplaceable

inline val IBlockState.isFullBox: Boolean
    get() = Wrapper.world?.let {
        this.getCollisionBoundingBox(it, BlockPos.ORIGIN)
    } == Block.FULL_BLOCK_AABB
