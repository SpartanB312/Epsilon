package club.eridani.epsilon.client.mixin.mixins.network;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    /*
    private static WorldServer world;

    @ModifyVariable(method = "loadAllWorlds", at = @At(value = "STORE", ordinal = 0))
    public WorldServer a(WorldServer in) {
        world = in;
        return in;
    }

    @Inject(method = "loadAllWorlds", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;post(Lnet/minecraftforge/fml/common/eventhandler/Event;)Z", shift = At.Shift.BEFORE),remap = false)
    public void a(String p_71247_1_, String p_71247_2_, long p_71247_3_, WorldType p_71247_5_, String p_71247_6_, CallbackInfo ci) {
        if (world != null && world.isRemote) {
            WorldEvent.Load.INSTANCE.post();
            world.addEventListener(WorldManager.INSTANCE);
        }
    }

    @Redirect(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;flush()V"))
    public void b(WorldServer worldServer) {
        if (world != null && world.isRemote){
            WorldEvent.Unload.INSTANCE.post();
            world.removeEventListener(WorldManager.INSTANCE);
        }
        worldServer.flush();
    }

     */
}
