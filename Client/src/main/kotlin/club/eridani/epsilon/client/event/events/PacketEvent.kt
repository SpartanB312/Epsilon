package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable
import net.minecraft.network.Packet

sealed class PacketEvent(val packet: Packet<*>?) : Cancellable() {
    class Receive(packet: Packet<*>?) : PacketEvent(packet)

    class PostReceive(packet: Packet<*>?) : PacketEvent(packet)

    class Send(packet: Packet<*>?) : PacketEvent(packet)

    class PostSend(packet: Packet<*>?) : PacketEvent(packet)
}