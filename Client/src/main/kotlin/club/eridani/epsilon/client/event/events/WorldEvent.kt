package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

sealed class WorldEvent : Cancellable() {

    object Unload : WorldEvent()
    object Load : WorldEvent()

    sealed class Entity(val entity: net.minecraft.entity.Entity) : WorldEvent() {
        class Add(entity: net.minecraft.entity.Entity) : Entity(entity)
        class Remove(entity: net.minecraft.entity.Entity) : Entity(entity)
    }

    class BlockUpdate(
        val pos: BlockPos,
        val oldState: IBlockState,
        val newState: IBlockState
    ) : WorldEvent()

    class RenderUpdate(
        val x1: Int,
        val y1: Int,
        val z1: Int,
        val x2: Int,
        val y2: Int,
        val z2: Int
    ) : WorldEvent()
}
