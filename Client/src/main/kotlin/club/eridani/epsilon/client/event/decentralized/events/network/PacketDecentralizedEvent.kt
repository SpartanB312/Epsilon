package club.eridani.epsilon.client.event.decentralized.events.network

import club.eridani.epsilon.client.event.decentralized.CancellableEventData
import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import net.minecraft.network.Packet

object PacketDecentralizedEvent {

    data class PacketEventData(val packet: Packet<*>?, override val father: DataDecentralizedEvent<*>) :
        CancellableEventData(father)

    object Send : DataDecentralizedEvent<PacketEventData>()

    object Receive : DataDecentralizedEvent<PacketEventData>()

    object PostSend : DataDecentralizedEvent<PacketEventData>()

    object PostReceive : DataDecentralizedEvent<PacketEventData>()
}