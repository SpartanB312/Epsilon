package club.eridani.epsilon.client.event.decentralized.events.client

import club.eridani.epsilon.client.event.decentralized.CancellableEventData
import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand

object RenderItemAnimationDecentralizedEvent :
    DataDecentralizedEvent<RenderItemAnimationDecentralizedEvent.RenderItemAnimationData>() {
    class RenderItemAnimationData(
        val stack: ItemStack,
        val hand: EnumHand,
        val coordinate: Float,
        override val father: DataDecentralizedEvent<*>
    ) :
        CancellableEventData(this)

    object Transform : DataDecentralizedEvent<RenderItemAnimationData>()
    object Render : DataDecentralizedEvent<RenderItemAnimationData>()
}