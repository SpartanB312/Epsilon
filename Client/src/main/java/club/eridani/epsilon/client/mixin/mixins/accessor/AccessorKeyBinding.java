package club.eridani.epsilon.client.mixin.mixins.accessor;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyBinding.class)
public interface AccessorKeyBinding {
    @Invoker("unpressKey")
    void epsilon$invoke$unpressKey();

    @Accessor("pressTime")
    int getPressTime();

    @Accessor("pressTime")
    void setPressTime(int value);
}
