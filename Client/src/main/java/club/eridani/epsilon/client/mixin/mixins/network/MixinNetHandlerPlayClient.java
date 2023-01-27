package club.eridani.epsilon.client.mixin.mixins.network;


import club.eridani.epsilon.client.module.misc.ClientSpoof;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketJoinGame;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Shadow
    @Final
    private NetworkManager netManager;

    @Inject(method = "handleJoinGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"))
    public void injectRegisterPacket(SPacketJoinGame packetIn, CallbackInfo ci) {
        if (ClientSpoof.INSTANCE.isEnabled()) {
            final ByteBuf message = Unpooled.buffer();
            message.writeBytes((ClientSpoof.INSTANCE.getClient() == ClientSpoof.Client.Lunar ? "Lunar-Client" : "vanilla").getBytes());
            this.netManager.sendPacket(new CPacketCustomPayload("REGISTER", new PacketBuffer(message)));
            return;
        }
    }

}
