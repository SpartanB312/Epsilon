package club.eridani.epsilon.client.mixin.mixins.accessor;

import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface AccessorTimer {
    @Accessor("tickLength")
    float epsilonGetTickLength();

    @Accessor("tickLength")
    void epsilonSetTickLength(float value);
}
