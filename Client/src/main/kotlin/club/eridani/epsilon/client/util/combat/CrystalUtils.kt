package club.eridani.epsilon.client.util.combat

import club.eridani.epsilon.client.common.extensions.isLiquid
import club.eridani.epsilon.client.common.extensions.isReplaceable
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.management.EntityManager
import club.eridani.epsilon.client.util.math.fastFloor
import club.eridani.epsilon.client.util.math.VectorUtils.setAndAdd
import net.minecraft.block.state.IBlockState
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.function.Predicate
import kotlin.math.abs

@Suppress("NOTHING_TO_INLINE")
object CrystalUtils {

    @JvmStatic
    inline fun isPlaceable(world: WorldClient, pos: BlockPos, newPlacement: Boolean): Boolean {
        if (!isValidBasePos(world, pos)) {
            return false
        }
        val posUp = pos.up(1)
        return if (newPlacement) {
            world.isAirBlock(posUp)
        } else {
            isValidSpace(world, posUp) && isValidSpace(world, posUp.up())
        }
    }

    @JvmStatic
    inline fun isValidBasePos(world: WorldClient, pos: BlockPos): Boolean {
        val block = world.getBlockState(pos).block
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN
    }

    @JvmStatic
    inline fun isValidSpace(world: WorldClient, pos: BlockPos): Boolean {
        val material = world.getBlockState(pos).material
        return !material.isLiquid && material.isReplaceable
    }


    val EntityEnderCrystal.blockPos: BlockPos
        get() = BlockPos(this.posX.fastFloor(), this.posY.fastFloor() - 1, this.posZ.fastFloor())

    private val mutableBlockPos = ThreadLocal.withInitial {
        BlockPos.MutableBlockPos()
    }

    /** Checks colliding with blocks and given entity */
    fun SafeClientEvent.canPlaceCrystal(pos: BlockPos, entity: EntityLivingBase? = null): Boolean {
        return canPlaceCrystalOn(pos)
                && (entity == null || !getCrystalPlacingBB(pos).intersects(entity.entityBoundingBox))
                && hasValidSpaceForCrystal(pos)
    }

    /** Checks if the block is valid for placing crystal */
    fun SafeClientEvent.canPlaceCrystalOn(pos: BlockPos): Boolean {
        val block = world.getBlockState(pos).block
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN
    }

    fun SafeClientEvent.hasValidSpaceForCrystal(pos: BlockPos): Boolean {
        val mutableBlockPos = mutableBlockPos.get()
        return isValidMaterial(world.getBlockState(mutableBlockPos.setAndAdd(pos, 0, 1, 0)))
                && isValidMaterial(world.getBlockState(mutableBlockPos.add(0, 1, 0)))
    }

    fun isValidMaterial(blockState: IBlockState): Boolean {
        return !blockState.isLiquid && blockState.isReplaceable
    }

    fun getCrystalPlacingBB(pos: BlockPos): AxisAlignedBB {
        return getCrystalPlacingBB(pos.x, pos.y, pos.z)
    }

    fun getCrystalPlacingBB(x: Int, y: Int, z: Int): AxisAlignedBB {
        return AxisAlignedBB(
            x + 0.001, y + 1.0, z + 0.001,
            x + 0.999, y + 3.0, z + 0.999
        )
    }

    fun getCrystalPlacingBB(pos: Vec3d): AxisAlignedBB {
        return getCrystalPlacingBB(pos.x, pos.y, pos.z)
    }

    fun getCrystalPlacingBB(x: Double, y: Double, z: Double): AxisAlignedBB {
        return AxisAlignedBB(
            x - 0.499, y, z - 0.499,
            x + 0.499, y + 2.0, z + 0.499
        )
    }

    fun getCrystalBB(pos: BlockPos): AxisAlignedBB {
        return getCrystalBB(pos.x, pos.y, pos.z)
    }

    fun getCrystalBB(x: Int, y: Int, z: Int): AxisAlignedBB {
        return AxisAlignedBB(
            x - 0.5, y + 1.0, z - 0.5,
            x + 1.5, y + 3.0, z + 1.5
        )
    }

    fun getCrystalBB(pos: Vec3d): AxisAlignedBB {
        return getCrystalBB(pos.x, pos.y, pos.z)
    }

    fun getCrystalBB(x: Double, y: Double, z: Double): AxisAlignedBB {
        return AxisAlignedBB(
            x - 1.0, y, z - 1.0,
            x + 1.0, y + 2.0, z + 1.0
        )
    }

    fun placeBoxIntersectsCrystalBox(placePos: BlockPos, crystalPos: BlockPos): Boolean {
        return crystalPos.y - placePos.y in 0..2
                && abs(crystalPos.x - placePos.x) < 2
                && abs(crystalPos.z - placePos.z) < 2
    }

    fun placeBoxIntersectsCrystalBox(placePos: Vec3d, crystalPos: BlockPos): Boolean {
        return crystalPos.y - placePos.y in 0.0..2.0
                && abs(crystalPos.x - placePos.x) < 2.0
                && abs(crystalPos.z - placePos.z) < 2.0
    }

    fun placeBoxIntersectsCrystalBox(placeX: Double, placeY: Double, placeZ: Double, crystalPos: BlockPos): Boolean {
        return crystalPos.y - placeY in 0.0..2.0
                && abs(crystalPos.x - placeX) < 2.0
                && abs(crystalPos.z - placeZ) < 2.0
    }

    fun placeBoxIntersectsCrystalBox(placeX: Double, placeY: Double, placeZ: Double, crystalX: Double, crystalY: Double, crystalZ: Double): Boolean {
        return crystalY - placeY in 0.0..2.0
                && abs(crystalX - placeX) < 2.0
                && abs(crystalZ - placeZ) < 2.0
    }

    /** Checks colliding with All Entities */
    fun placeCollideCheck(pos: BlockPos): Boolean {
        val placingBB = getCrystalPlacingBB(pos)
        return EntityManager.entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.entityBoundingBox.intersects(placingBB) }
            .none()
    }

    fun placeCollideCheck(pos: BlockPos, predicate: Predicate<Entity>): Boolean {
        val placingBB = getCrystalPlacingBB(pos)
        return EntityManager.entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.entityBoundingBox.intersects(placingBB) }
            .filterNot { predicate.test(it) }
            .none()
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
    fun isResistant(blockState: IBlockState) =
        !blockState.isLiquid && blockState.block.getExplosionResistance(null) >= 19.7

}