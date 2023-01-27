package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.common.extensions.syncCurrentPlayItems
import club.eridani.epsilon.client.common.interfaces.Helper
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.network.play.client.CPacketHeldItemChange

object ItemUtil : Helper {

    val blackList: List<Block> = listOf(
        Blocks.ENDER_CHEST,
        Blocks.CHEST,
        Blocks.TRAPPED_CHEST,
        Blocks.CRAFTING_TABLE,
        Blocks.ANVIL,
        Blocks.BREWING_STAND,
        Blocks.HOPPER,
        Blocks.DROPPER,
        Blocks.DISPENSER,
        Blocks.TRAPDOOR
    )

    val shulkersList: List<Block> = listOf(
        Blocks.WHITE_SHULKER_BOX,
        Blocks.ORANGE_SHULKER_BOX,
        Blocks.MAGENTA_SHULKER_BOX,
        Blocks.LIGHT_BLUE_SHULKER_BOX,
        Blocks.YELLOW_SHULKER_BOX,
        Blocks.LIME_SHULKER_BOX,
        Blocks.PINK_SHULKER_BOX,
        Blocks.GRAY_SHULKER_BOX,
        Blocks.SILVER_SHULKER_BOX,
        Blocks.CYAN_SHULKER_BOX,
        Blocks.PURPLE_SHULKER_BOX,
        Blocks.BLUE_SHULKER_BOX,
        Blocks.BROWN_SHULKER_BOX,
        Blocks.GREEN_SHULKER_BOX,
        Blocks.RED_SHULKER_BOX,
        Blocks.BLACK_SHULKER_BOX
    )

    fun getBlockInHotBar(): Int {
        var slot = if (mc.player.heldItemMainhand.item is ItemBlock) mc.player.inventory.currentItem else -1
        if (slot == -1) {
            for (i in 0..8) {
                if (Wrapper.mc.player.inventory.getStackInSlot(i).item is ItemBlock) {
                    slot = i
                    break
                }
            }
        }
        return slot
    }

    fun getItemCount(item: Item): Int {
        var count = Minecraft.getMinecraft().player.inventory.mainInventory.stream()
            .filter { itemStack: ItemStack -> itemStack.item === item }
            .mapToInt { obj: ItemStack -> obj.count }.sum()
        if (Minecraft.getMinecraft().player.heldItemOffhand.item === item) {
            ++count
        }
        return count
    }

    fun getPickaxeInHotBar(): Int {
        var slot = if (mc.player.heldItemMainhand.item is ItemPickaxe) mc.player.inventory.currentItem else -1
        if (slot == -1) {
            for (i in 0..8) {
                if (Wrapper.mc.player.inventory.getStackInSlot(i).item is ItemPickaxe) {
                    slot = i
                    break
                }
            }
        }
        return slot
    }


    fun getSwordInHotBar(): Int {
        var slot = if (mc.player.heldItemMainhand.item is ItemSword) mc.player.inventory.currentItem else -1
        if (slot == -1) {
            for (i in 0..8) {
                if (Wrapper.mc.player.inventory.getStackInSlot(i).item is ItemSword) {
                    slot = i
                    break
                }
            }
        }
        return slot
    }

    fun findBlockInHotBar(block: Block?): Int {
        return findItemInHotBar(Item.getItemFromBlock(block))
    }

    fun findItemInHotBar(item: Item): Int {
        for (i in 0..8) {
            val itemStack = mc.player.inventory.getStackInSlot(i)
            if (itemStack.item === item) {
                return i
            }
        }
        return -1
    }

    fun switchToSlot(slot: Int) {
        if (mc.player.inventory.currentItem == slot
            || slot == -1
        ) {
            return
        }
        mc.player.connection.sendPacket(CPacketHeldItemChange(slot))
        mc.player.inventory.currentItem = slot
        mc.playerController.updateController()
    }

    fun swapToSlot(slot: Int) {
        if (mc.player.inventory.currentItem == slot
            || slot == -1
        ) {
            return
        }
        mc.player.inventory.currentItem = slot
        mc.playerController.syncCurrentPlayItems()
    }
}