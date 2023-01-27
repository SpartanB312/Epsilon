package club.eridani.epsilon.client.mixin.mixins.accessor.render;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface AccessorEntityRenderer {
    @Invoker("setupCameraTransform")
    void invokeSetupCameraTransform(float partialTicks, int pass);
}
