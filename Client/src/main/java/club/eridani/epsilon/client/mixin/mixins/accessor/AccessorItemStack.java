package club.eridani.epsilon.client.mixin.mixins.accessor;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface AccessorItemStack {
    @Accessor("stackSize")
    int getStackSize();

    @Accessor("stackSize")
    void setStackSize(int input);
}
