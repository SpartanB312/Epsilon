package club.eridani.epsilon.client.mixin.mixins.accessor.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface AccessorEntity {
    @Accessor("isInWeb")
    boolean epsilonIsInWeb();

    @Invoker("getFlag")
    boolean epsilonGetFlag(int flag);

    @Invoker("setFlag")
    void epsilonSetFlag(int flag, boolean value);
}
