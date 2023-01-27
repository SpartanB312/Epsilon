package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.util.world.getBlock
import net.minecraft.client.Minecraft
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos


@Suppress("NOTHING_TO_INLINE")
object Utils : Helper {


    fun getPlaceableSide(pos: BlockPos): EnumFacing? {
        for (side in EnumFacing.values()) {
            val neighbour = pos.offset(side)
            if (!mc.world.getBlockState(neighbour).block.canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                continue
            }
            val blockState = mc.world.getBlockState(neighbour)
            if (!blockState.material.isReplaceable) {
                return side
            }
        }
        return null
    }

    fun canBeClicked(pos: BlockPos): Boolean {
        return mc.world.getBlock(pos).canCollideCheck(mc.world.getBlockState(pos), false)
    }

    inline fun <E : Enum<E>> E.next(): E = declaringClass.enumConstants.run {
        get((ordinal + 1) % size)
    }

    inline fun <E : Enum<E>> E.last(): E = declaringClass.enumConstants.run {
        get(if (ordinal == 0) size - 1 else ordinal - 1)
    }

    fun isInHole(pos: BlockPos, holeheight: Boolean): Boolean {
        if (pos.y > 125.0) {
            return false
        }
        val isSolid = (!mc.world.getBlockState(pos).material.blocksMovement() && !mc.world.getBlockState(pos.add(0, 1, 0)).material.blocksMovement() && (!mc.world.getBlockState(pos.add(0, 2, 0)).material.blocksMovement() || !holeheight) && mc.world.getBlockState(pos.add(0, -1, 0)).material.isSolid && mc.world.getBlockState(pos.add(1, 0, 0)).material.isSolid && mc.world.getBlockState(pos.add(0, 0, 1)).material.isSolid && mc.world.getBlockState(pos.add(-1, 0, 0)).material.isSolid && mc.world.getBlockState(pos.add(0, 0, -1)).material.isSolid)
        val isBedrock = ((mc.world.getBlockState(pos.add(0, -1, 0)).block.equals(Blocks.BEDROCK) || mc.world.getBlockState(pos.add(0, -1, 0)).block.equals(Blocks.OBSIDIAN)) && (mc.world.getBlockState(pos.add(1, 0, 0)).block.equals(Blocks.BEDROCK) || mc.world.getBlockState(pos.add(1, 0, 0)).block.equals(Blocks.OBSIDIAN)) && (mc.world.getBlockState(pos.add(0, 0, 1)).block.equals(Blocks.BEDROCK) || mc.world.getBlockState(pos.add(0, 0, 1)).block.equals(Blocks.OBSIDIAN)) && (mc.world.getBlockState(pos.add(-1, 0, 0)).block.equals(Blocks.BEDROCK) || mc.world.getBlockState(pos.add(-1, 0, 0)).block.equals(Blocks.OBSIDIAN)) && (mc.world.getBlockState(pos.add(0, 0, -1)).block.equals(Blocks.BEDROCK) || mc.world.getBlockState(pos.add(0, 0, -1)).block.equals(Blocks.OBSIDIAN)))
        return isBedrock || isSolid
    }


    //Minecraft os gay
    inline fun getShaderGroup(): ShaderGroup? = Minecraft.getMinecraft().entityRenderer.shaderGroup

    inline fun nullCheck() = mc.world == null || mc.player == null

}