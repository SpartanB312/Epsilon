package club.eridani.epsilon.client.mixin.mixins.accessor.render;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderer.class)
public interface AccessorItemRenderer {
    @Accessor("prevEquippedProgressMainHand")
    float getPrevEquippedProgressMainHand();

    @Accessor("equippedProgressMainHand")
    float getEquippedProgressMainHand();

    @Accessor("equippedProgressMainHand")
    void setEquippedProgressMainHand(float mainHand);

    @Accessor("equippedProgressOffHand")
    float getEquippedProgressOffHand();

    @Accessor("equippedProgressOffHand")
    void setEquippedProgressOffHand(float offHand);

    @Accessor("itemStackMainHand")
    ItemStack getItemStackMainHand();

    @Accessor("itemStackMainHand")
    void setItemStackMainHand(ItemStack stack);
}
