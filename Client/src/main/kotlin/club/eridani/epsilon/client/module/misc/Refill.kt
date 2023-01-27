package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.onTick
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.block.Block
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack


object Refill : Module(name = "Refill", category = Category.Misc, alias = arrayOf("HotbarReplenish"), description = "Auto refill items which is on your hotbar") {
    private val type by setting("Type", Type.QuickMove)
    private val onInv = setting("Inventory Only", false)
    private val threshold = setting("Threshold", 32, 1..63, 1)
    private val tickDelay = setting("TickDelay", 2, 1..10, 1)
    private var delayStep = 0

    init {
        onTick {
            runSafe {
                if (!onInv.value || mc.currentScreen is GuiInventory) {
                    delayStep = if (delayStep < tickDelay.value) {
                        delayStep++
                        return@runSafe
                    } else {
                        0
                    }

                    val slots = findReplenishableHotbarSlot() ?: return@runSafe
                    val inventorySlot = slots.first
                    val hotbarSlot = slots.second

                    if (type == Type.QuickMove) {
                        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.QUICK_MOVE, mc.player)
                    } else {
                        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player)
                        mc.playerController.windowClick(0, hotbarSlot, 0, ClickType.PICKUP, mc.player)
                        mc.playerController.windowClick(0, inventorySlot, 0, ClickType.PICKUP, mc.player)
                    }
                }
            }
        }
    }

    override fun onDisable() {
        delayStep = 0
    }

    private fun getInventory(): Map<Int, ItemStack> {
        return getInventorySlots(9, 35)
    }

    private fun getHotbar(): Map<Int, ItemStack> {
        return getInventorySlots(36, 44)
    }

    private fun getInventorySlots(current: Int, last: Int): Map<Int, ItemStack> {
        var current: Int = current
        val fullInventorySlots: MutableMap<Int, ItemStack> = mutableMapOf()
        while (current <= last) {
            fullInventorySlots[current] = mc.player.inventoryContainer.inventory[current]
            current++
        }
        return fullInventorySlots
    }

    private fun findReplenishableHotbarSlot(): Pair<Int, Int>? {
        var returnPair: Pair<Int, Int>? = null
        for ((key, stack) in getHotbar()) {
            if (stack.isEmpty || stack.item == Items.AIR) continue
            if (!stack.isStackable) continue
            if (stack.count >= stack.maxStackSize) continue
            if (stack.count > threshold.value) continue

            val inventorySlot: Int = findCompatibleInventorySlot(stack)
            if (inventorySlot == -1) {
                continue
            }
            returnPair = Pair(inventorySlot, key)
        }
        return returnPair
    }

    private fun findCompatibleInventorySlot(hotbarStack: ItemStack): Int {
        var inventorySlot = -1
        var smallestStackSize = 999
        for ((key, inventoryStack) in getInventory()) {
            if (inventoryStack.isEmpty || inventoryStack.item == Items.AIR) {
                continue
            }
            if (!isCompatibleStacks(hotbarStack, inventoryStack)) {
                continue
            }
            val currentStackSize = mc.player.inventoryContainer.inventory[key].count
            if (smallestStackSize > currentStackSize) {
                smallestStackSize = currentStackSize
                inventorySlot = key
            }
        }
        return inventorySlot
    }

    private fun isCompatibleStacks(stack1: ItemStack, stack2: ItemStack): Boolean {
        if (stack1.item != stack2.item) {
            return false
        }

        if (stack1.item is ItemBlock && stack2.item is ItemBlock) {
            val block1: Block = (stack1.item as ItemBlock).block
            val block2: Block = (stack2.item as ItemBlock).block

            if (!block1.defaultState.material.equals(block2.defaultState.material)) {
                return false
            }
        }

        if (stack1.displayName != stack2.displayName) {
            return false
        }
        return stack1.itemDamage == stack2.itemDamage
    }

    enum class Type {
        PickUp, QuickMove
    }
}