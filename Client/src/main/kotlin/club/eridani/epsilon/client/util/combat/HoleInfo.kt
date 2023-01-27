package club.eridani.epsilon.client.util.combat

import club.eridani.epsilon.client.util.math.vector.toVec3d
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class HoleInfo(
    val origin: BlockPos,
    val center: Vec3d,
    val boundingBox: AxisAlignedBB,
    val holePos: Array<BlockPos>,
    val surroundPos: Array<BlockPos>,
    val type: HoleType,
    val isTrapped: Boolean,
) {
    val isHole = type != HoleType.NONE
    val isSafe = type == HoleType.BEDROCK
    val isTwo = type == HoleType.TWO
    val isFour = type == HoleType.FOUR

    fun canEnter(world: World, pos: BlockPos): Boolean {
        val headPosY = pos.y + 2
        if (origin.y >= headPosY) return false
        val box = boundingBox.expand(0.0, headPosY - origin.y - 1.0, 0.0)
        return !world.collidesWithAnyBlock(box)
    }

    override fun equals(other: Any?) =
        this === other
            || other is HoleInfo
            && origin == other.origin

    override fun hashCode() =
        origin.hashCode()

    companion object {
        fun empty(pos: BlockPos) =
            HoleInfo(
                pos,
                pos.toVec3d(0.5, 0.0, 0.5),
                AxisAlignedBB(pos),
                emptyBlockPosArray,
                emptyBlockPosArray,
                HoleType.NONE,
                false
            )

        private val emptyBlockPosArray = emptyArray<BlockPos>()
    }
}