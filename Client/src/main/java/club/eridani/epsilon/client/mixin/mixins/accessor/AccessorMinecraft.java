package club.eridani.epsilon.client.mixin.mixins.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {

    @Accessor("session")
    void setSession(Session session);

    @Accessor("session")
    Session getSession();

    @Accessor("timer")
    Timer epsilonGetTimer();

    @Accessor("renderPartialTicksPaused")
    float getRenderPartialTicksPaused();

    @Accessor("rightClickDelayTimer")
    int getRightClickDelayTimer();

    @Accessor("rightClickDelayTimer")
    void setRightClickDelayTimer(int value);

    @Invoker("rightClickMouse")
    void invokeRightClickMouse();

    @Invoker("sendClickBlockToController")
    void invokeSendClickBlockToController(boolean leftClick);

}
