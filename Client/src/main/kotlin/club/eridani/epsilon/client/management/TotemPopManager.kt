package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.event.events.TotemPopEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.combat.TotemPopCounter
import club.eridani.epsilon.client.notification.Notification
import club.eridani.epsilon.client.notification.NotificationManager
import club.eridani.epsilon.client.notification.NotificationType
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.onPacketReceive
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus


object TotemPopManager : Helper {
    private val popList = mutableMapOf<String, Int>()

    init {
        onPacketReceive { event ->
            runSafe {
                if (event.packet is SPacketEntityStatus && event.packet.opCode.toInt() == 35) {
                    club.eridani.epsilon.client.event.EventBus.post(TotemPopEvent(event.packet.getEntity(mc.world)))
                }
            }
        }

        listener<OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData> {
            runSafe {
                for (player in mc.world.playerEntities) {
                    if (player.health <= 0) {
                        if (popList.containsKey(player.name)) {
                            popList[player.name]?.let { notify(player, it, true) }
                            popList.remove(player.name, popList[player.name])
                        }
                    }
                }
            }
        }

        listener<TotemPopEvent> { event ->
            runSafe {
                if (event.entity is EntityPlayer) {
                    if (popList[event.entity.getName()] == null) {
                        popList[event.entity.getName()] = 1
                        notify(event.entity, 1, false)
                    } else if (popList[event.entity.getName()] != null) {
                        val popCounter = popList[event.entity.getName()]!!
                        val newPopCounter = popCounter + 1
                        popList[event.entity.getName()] = newPopCounter
                        notify(event.entity, newPopCounter, false)
                    }
                }
            }
        }
    }


    private fun notify(entity: EntityPlayer, count: Int, dead: Boolean) {
        val entityId = entity.entityId
        val name = if (entity.name == mc.player.name) "You" else entity.name
        val firstColor: String = GUIManager.firstTextColor
        val primaryColor: String = GUIManager.primaryTextColor
        val grammar = if (count > 1) " totems!" else " totem!"
        if (dead) {
            val message = StringBuilder("$firstColor$name died after popping $primaryColor$count$firstColor$grammar")
            if (name == "You") message.append(" You are so bad nigga.")
            sendMessage(message.toString(), entityId)
        } else sendMessage("$firstColor$name popped $primaryColor$count$firstColor$grammar", entityId)
    }

    private fun sendMessage(message: String, code: Int) {
        if (TotemPopCounter.isEnabled) {
            val shouldUseNotification = TotemPopCounter.notification
            val shouldChatMessage = TotemPopCounter.chat
            if (shouldUseNotification)  NotificationManager.show(
                Notification(
                    message = message.replace(GUIManager.firstTextColor, "").replace(GUIManager.primaryTextColor, ""),
                    type = NotificationType.INFO
                )
            )
            if (shouldChatMessage) {
                if (TotemPopCounter.rawChat) ChatUtil.sendNoSpamRawChatMessage(message, code) else ChatUtil.sendNoSpamMessage(message, code)
            }
        }
    }

    fun getTotemPops(player: EntityPlayer): Int? {
        return if (popList[player.name] == null) {
            0
        } else popList[player.name]
    }
}