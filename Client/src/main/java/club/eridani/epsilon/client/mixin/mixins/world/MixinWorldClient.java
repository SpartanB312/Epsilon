package club.eridani.epsilon.client.mixin.mixins.world;


import club.eridani.epsilon.client.event.SafeClientEvent;
import club.eridani.epsilon.client.event.events.ConnectionEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World {

    protected MixinWorldClient(ISaveHandler p_i45749_1_, WorldInfo p_i45749_2_, WorldProvider p_i45749_3_, Profiler p_i45749_4_, boolean p_i45749_5_) {
        super(p_i45749_1_, p_i45749_2_, p_i45749_3_, p_i45749_4_, p_i45749_5_);
    }

    /*
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void a(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_, CallbackInfo ci) {
        WorldEvent.Load.INSTANCE.post();
        this.addEventListener(WorldManager.INSTANCE);
    }

     */

    @Inject(method = "sendQuittingDisconnectingPacket", at = @At("HEAD"))
    private void onPreSendQuittingDisconnectingPacket(CallbackInfo ci) {
        SafeClientEvent.Companion.update();
        ConnectionEvent.Disconnect.INSTANCE.post();
    }
}
