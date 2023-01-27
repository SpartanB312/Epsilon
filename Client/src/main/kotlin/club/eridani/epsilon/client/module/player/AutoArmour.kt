package club.eridani.epsilon.client.module.player

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.text.ChatUtil.sendNoSpamMessage
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.Item
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack


object AutoArmour : Module(name = "AutoArmour", category = Category.Player, description = "Auto replace armour") {
    private val delay by setting("Delay", 50, 0..1000, 1)
    private val expPause by setting("ExpPause", false)
    private val mendingTakeOff by setting("ExpMend", false)
    private val takeOffOnly by setting("TakeOff Only", false) { mendingTakeOff }
    private val durability by setting("Durability", 80, 0..100, 1) { mendingTakeOff }
    private val curse by setting("Cures", false)
    private var body by setting("Body", Mode.Chest)
    private val elytry by setting("Elytry Durability", 20, 0..100, 1) { body == Mode.Elytra }


    val timer = Timer()
    private val isSafe: Boolean
        get() {
            if (Utils.nullCheck()) return false
            mc.world.playerEntities.forEach {
                if (it == mc.player) return@forEach
                if (mc.player.getDistance(it) < 6) return false
            }
            return true
        }

    init {
        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            if (mc.currentScreen is GuiInventory) {
                return@decentralizedListener
            }

            if (expPause && mc.player.heldItemMainhand.item === Items.EXPERIENCE_BOTTLE) {
                return@decentralizedListener
            }

//            if (InvCleaner.isCleaning) {
//                return
//            }

            val helm = mc.player.inventoryContainer.getSlot(5).stack
            val chest = mc.player.inventoryContainer.getSlot(6).stack
            val legging = mc.player.inventoryContainer.getSlot(7).stack
            val feet = mc.player.inventoryContainer.getSlot(8).stack

            val shouldMend = (mendingTakeOff
                    && mc.player.heldItemMainhand.item === Items.EXPERIENCE_BOTTLE && mc.gameSettings.keyBindUseItem.isKeyDown
                    && isSafe)

            if (shouldMend) {
                for (i in 0..3) {
                    val armor = mc.player.inventory.armorItemInSlot(i)
                    if (getDmg(armor) < durability || armor == ItemStack.EMPTY) continue
                    if (timer.passed(delay)) {
                        mc.playerController.windowClick(
                            mc.player.inventoryContainer.windowId,
                            8 - i,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                        )
                        timer.reset()
                    }
                }
            }


            if (helm.item == Items.AIR) {
                val slot = findArmorSlot(EntityEquipmentSlot.HEAD, shouldMend)
                if (slot != -1) {
                    clickSlot(slot)
                }
            }

            val chestDmg = getDmg(chest)

            if (body == Mode.Chest) {
                if (chest.item == Items.AIR) {
                    val slot = findArmorSlot(EntityEquipmentSlot.CHEST, shouldMend)
                    if (slot != -1) {
                        replaceSlot(6, slot)
                    }
                }
            } else {
                val elytraCount: Int = countItems(Items.ELYTRA) + countArmor(Items.ELYTRA)
                if (elytraCount != 0) {
                    val elytraslot = getElytraSlot(shouldMend)
                    if (elytraslot != -1) {
                        if (mc.player.inventory.armorInventory[2].isEmpty) {
                            clickSlot(elytraslot)
                        } else if (!mc.player.inventory.armorInventory[2].item.equals(Items.ELYTRA)
                            || mc.player.inventory.armorInventory[2].item.equals(
                                Items.ELYTRA
                            ) && chestDmg <= elytry
                        ) {
                            replaceSlot(6, elytraslot)
                        }
                    }
                } else {
                    sendNoSpamMessage("No elytra found! Auto set to CHEST mode")
                    body = Mode.Chest
                }
            }


            if (legging.item == Items.AIR) {
                val slot = findArmorSlot(EntityEquipmentSlot.LEGS, shouldMend)
                if (slot != -1) {
                    clickSlot(slot)
                }
            }

            if (feet.item === Items.AIR) {
                val slot = findArmorSlot(EntityEquipmentSlot.FEET, shouldMend)
                if (slot != -1) {
                    clickSlot(slot)
                }
            }
        }
    }


    private fun replaceSlot(placeSlot: Int, ItemSlot: Int) {
        if (timer.passed(delay)) {
            mc.playerController.windowClick(
                mc.player.inventoryContainer.windowId,
                ItemSlot,
                0,
                ClickType.PICKUP,
                mc.player
            )
            mc.playerController.windowClick(
                mc.player.inventoryContainer.windowId,
                placeSlot,
                0,
                ClickType.PICKUP,
                mc.player
            )
            mc.playerController.windowClick(
                mc.player.inventoryContainer.windowId,
                ItemSlot,
                0,
                ClickType.PICKUP,
                mc.player
            )
            mc.playerController.updateController()
            timer.reset()
        }
    }

    private fun clickSlot(slot: Int) {
        if (timer.passed(delay)) {
            mc.playerController.windowClick(
                mc.player.inventoryContainer.windowId,
                slot,
                0,
                ClickType.QUICK_MOVE,
                mc.player
            )
            timer.reset()
        }
    }

    private fun getElytraSlot(shouldMend: Boolean): Int {
        var i = 0
        while (i < 36) {
            val item = mc.player.inventory.getStackInSlot(i).item
            if (shouldMend) {
                if (getDmg(mc.player.inventory.getStackInSlot(i)) <= durability) {
                    i++
                    continue
                }
            }
            if (item == Items.ELYTRA) {
                if (i < 9) {
                    i += 36
                }
                return i
            }
            i++
        }
        return -1
    }

    private fun findArmorSlot(type: EntityEquipmentSlot, shouldMend: Boolean): Int {
        var slot = -1
        var damage = 0f
        for (i in 9..44) {
            val s = Minecraft.getMinecraft().player.inventoryContainer.getSlot(i).stack
            if (!s.isEmpty && s.item !== Items.AIR) {
                if (shouldMend) if (takeOffOnly) {
                    continue
                } else {
                    if (getDmg(s) <= durability) continue
                }
                if (s.item is ItemArmor) {
                    val armor = s.item as ItemArmor
                    if (armor.armorType == type) {
                        val currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(
                            Enchantments.PROTECTION,
                            s
                        )).toFloat()
                        val cursed = curse && EnchantmentHelper.hasBindingCurse(s)
                        if (currentDamage > damage && !cursed) {
                            damage = currentDamage
                            slot = i
                        }
                    }
                }
            }
        }
        return slot
    }

    private fun getDmg(armour: ItemStack): Double {
        val dmg = ((armour.maxDamage - armour.itemDamage.toFloat()) / armour.maxDamage).toDouble()
        val getPercentage = 1 - dmg
        return 100 - getPercentage * 100
    }

    private fun countArmor(item: Item): Int {
        return mc.player.inventory.armorInventory.filter { itemStack -> itemStack.item == item }
            .sumOf { obj: ItemStack -> obj.count }
    }

    private fun countItems(item: Item): Int {
        return mc.player.inventory.mainInventory.filter { itemStack -> itemStack.item === item }
            .sumOf { obj: ItemStack -> obj.count }
            .plus(mc.player.inventory.offHandInventory.filter { itemStack -> itemStack.item === item }
                .sumOf { obj: ItemStack -> obj.count })
    }

    enum class Mode {
        Elytra,
        Chest
    }

}