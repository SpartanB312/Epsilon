package club.eridani.epsilon.client.mixin.mixins.network;

import club.eridani.epsilon.client.event.EventBus;
import club.eridani.epsilon.client.event.decentralized.events.network.PacketDecentralizedEvent;
import club.eridani.epsilon.client.event.events.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void sendPacketPre(Packet<?> packet, CallbackInfo callbackInfo) {
        PacketDecentralizedEvent.PacketEventData data = new PacketDecentralizedEvent.PacketEventData(packet, PacketDecentralizedEvent.Send.INSTANCE);
        PacketDecentralizedEvent.Send.INSTANCE.post(data);

        PacketEvent.Send event = new PacketEvent.Send(packet);
        EventBus.post(event);

        if (data.getCancelled() || event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void channelReadPre(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketDecentralizedEvent.PacketEventData data = new PacketDecentralizedEvent.PacketEventData(packet, PacketDecentralizedEvent.Receive.INSTANCE);
        PacketDecentralizedEvent.Receive.INSTANCE.post(data);

        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        EventBus.post(event);

        if (data.getCancelled() || event.getCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("RETURN"))
    private void sendPacket$Inject$RETURN(Packet<?> packetIn, CallbackInfo ci) {
        PacketDecentralizedEvent.PacketEventData data = new PacketDecentralizedEvent.PacketEventData(packetIn, PacketDecentralizedEvent.PostSend.INSTANCE);
        PacketDecentralizedEvent.PostSend.INSTANCE.post(data);

        EventBus.post(new PacketEvent.PostSend(packetIn));
    }

    @Inject(method = "channelRead0", at = @At("RETURN"), cancellable = true)
    private void channelReadPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        PacketDecentralizedEvent.PacketEventData data = new PacketDecentralizedEvent.PacketEventData(packet, PacketDecentralizedEvent.PostSend.INSTANCE);
        PacketDecentralizedEvent.PostReceive.INSTANCE.post(data);

        EventBus.post(new PacketEvent.PostReceive(packet));
    }

}
