package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.common.extensions.currentPlayerItem
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.PacketEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.util.inventory.slot.HotbarSlot
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange

@Suppress("NOTHING_TO_INLINE")
object HotbarManager {
    var serverSideHotbar = 0; private set
    var swapTime = 0L; private set

    val EntityPlayerSP.serverSideItem: ItemStack
        get() = inventory.mainInventory[serverSideHotbar]

    init {
        safeListener<PacketEvent.Send>(Int.MIN_VALUE) {
            if (it.cancelled || it.packet !is CPacketHeldItemChange) return@safeListener

            synchronized(playerController) {
                serverSideHotbar = it.packet.slotId
                swapTime = System.currentTimeMillis()
            }
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot, block: () -> Unit) {
        //contract {
        //    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        //}

        synchronized(playerController) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int, block: () -> Unit) {
        //contract {
        //    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        //}

        synchronized(playerController) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot) {
        return spoofHotbar(slot.hotbarSlot)
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int) {
        if (club.eridani.epsilon.client.management.HotbarManager.serverSideHotbar != slot) {
            connection.sendPacket(CPacketHeldItemChange(slot))
        }
    }

    inline fun SafeClientEvent.resetHotbar() {
        val slot = playerController.currentPlayerItem
        if (club.eridani.epsilon.client.management.HotbarManager.serverSideHotbar != slot) {
            spoofHotbar(slot)
        }
    }
}