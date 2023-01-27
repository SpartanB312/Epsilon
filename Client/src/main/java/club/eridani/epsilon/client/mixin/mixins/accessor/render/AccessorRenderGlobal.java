package club.eridani.epsilon.client.mixin.mixins.accessor.render;

import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderGlobal.class)
public interface AccessorRenderGlobal {
    @Accessor("entityOutlineShader")
    ShaderGroup getEntityOutlineShader();

    @Accessor("damagedBlocks")
    Map<Integer, DestroyBlockProgress> epsilonGetDamagedBlocks();


    @Accessor("damagedBlocks")
    Map<Integer, DestroyBlockProgress> getDamagedBlocks();

    @Accessor("renderEntitiesStartupCounter")
    int epsilonGetRenderEntitiesStartupCounter();

    @Accessor("renderEntitiesStartupCounter")
    void epsilonSetRenderEntitiesStartupCounter(int value);

    @Accessor("countEntitiesTotal")
    int epsilonGetCountEntitiesTotal();

    @Accessor("countEntitiesTotal")
    void epsilonSetCountEntitiesTotal(int value);

    @Accessor("countEntitiesRendered")
    int epsilonGetCountEntitiesRendered();

    @Accessor("countEntitiesRendered")
    void epsilonSetCountEntitiesRendered(int value);

    @Accessor("countEntitiesHidden")
    int epsilonGetCountEntitiesHidden();

    @Accessor("countEntitiesHidden")
    void epsilonSetCountEntitiesHidden(int value);
}
