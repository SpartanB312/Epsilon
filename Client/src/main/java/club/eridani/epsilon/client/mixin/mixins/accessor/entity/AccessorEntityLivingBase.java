package club.eridani.epsilon.client.mixin.mixins.accessor.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityLivingBase.class)
public interface AccessorEntityLivingBase {
    @Accessor("HEALTH")
    static DataParameter<Float> epsilonGetHealthDataKey() {
        throw new AssertionError();
    }

    @Invoker("onItemUseFinish")
    void epsilonInvokeOnItemUseFinish();
}
