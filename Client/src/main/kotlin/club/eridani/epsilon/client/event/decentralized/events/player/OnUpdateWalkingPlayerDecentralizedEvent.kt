package club.eridani.epsilon.client.event.decentralized.events.player

import club.eridani.epsilon.client.event.decentralized.CancellableEventData
import club.eridani.epsilon.client.event.decentralized.DataDecentralizedEvent
import club.eridani.epsilon.client.management.PlayerPacketManager
import club.eridani.epsilon.client.util.math.Vec2f
import net.minecraft.util.math.Vec3d

object OnUpdateWalkingPlayerDecentralizedEvent :
    DataDecentralizedEvent<OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData>() {
    class OnUpdateWalkingPlayerData(
        position: Vec3d,
        rotation: Vec2f,
        onGround: Boolean,
        override val father: DataDecentralizedEvent<*>
    ) : CancellableEventData(this) {

        var position = position; private set
        var rotation = rotation; private set

        var onGround = onGround
            @JvmName("isOnGround") get
            private set

        var cancelMove = false; private set
        var cancelRotate = false; private set
        var cancelAll = false; private set

        fun apply(packet: PlayerPacketManager.Packet) {
            cancel()

            packet.position?.let {
                this.position = it
            }
            packet.rotation?.let {
                this.rotation = it
            }
            packet.onGround?.let {
                this.onGround = it
            }

            this.cancelMove = packet.cancelMove
            this.cancelRotate = packet.cancelRotate
            this.cancelAll = packet.cancelAll
        }
    }

    object Pre : DataDecentralizedEvent<OnUpdateWalkingPlayerData>()

    object Post : DataDecentralizedEvent<OnUpdateWalkingPlayerData>()

}