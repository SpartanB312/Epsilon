package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.runSafeTask
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.onPacketReceive
import club.eridani.epsilon.client.util.onPacketSend
import club.eridani.epsilon.client.util.onTick
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketAnimation
import net.minecraft.network.play.server.SPacketEntity
import net.minecraft.scoreboard.ScorePlayerTeam
import java.util.regex.Pattern

object AntiBot : Module(name = "AntiBot", category = Category.Misc, description = "Prevent you hitting some fake player with detection in server") {
    val remove by setting("RemoveEntity", false)
    private val tabValue by setting("Tab", true)
    private val tabModeValue by setting("TabMode", TabMode.Contains)
    private val entityIDValue by setting("EntityID", true)
    private val colorValue by setting("Color", false)
    private val livingTimeValue by setting("LivingTime", false)
    private val livingTimeTicksValue by setting("LivingTimeTicks", 40, 1..200, 1)
    private val groundValue by setting("Ground", true)
    private val airValue by setting("Air", false)
    private val invalidGroundValue by setting("InvalidGround", true)
    private val swingValue by setting("Swing", false)
    private val healthValue by setting("Health", false)
    private val derpValue by setting("Derp", true)
    private val wasInvisibleValue by setting("WasInvisible", false)
    private val armorValue by setting("Armor", false)
    private val pingValue by setting("Ping", false)
    private val needHitValue by setting("NeedHit", false)
    private val duplicateInWorldValue by setting("DuplicateInWorld", false)
    private val duplicateInTabValue by setting("DuplicateInTab", false)

    private val COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]")
    private val ground = mutableListOf<Int>()
    private val air = mutableListOf<Int>()
    private val invalidGround = mutableMapOf<Int, Int>()
    private val swing = mutableListOf<Int>()
    private val invisible = mutableListOf<Int>()
    private val hitted = mutableListOf<Int>()


    init {
        onPacketSend { event ->
            runSafeTask {
                if (event.packet is CPacketUseEntity && event.packet.action == CPacketUseEntity.Action.ATTACK) {
                    val entity = event.packet.getEntityFromWorld(mc.world)
                    if (entity is EntityLivingBase && !hitted.contains(entity.getEntityId())) hitted.add(entity.getEntityId())
                }
            }
        }
        onPacketReceive { event ->
            runSafeTask {
                if (event.packet is SPacketEntity) {
                    val entity = event.packet.getEntity(mc.world)
                    if (entity is EntityPlayer) {
                        if (event.packet.onGround && !ground.contains(entity.getEntityId())) ground.add(entity.getEntityId())
                        if (!event.packet.onGround && !air.contains(entity.getEntityId())) air.add(entity.getEntityId())
                        if (event.packet.onGround) {
                            if (entity.prevPosY != entity.posY) invalidGround.put(entity.getEntityId(), (invalidGround[entity.getEntityId()] ?: 0) + 1)
                        } else {
                            val currentVL = (invalidGround[entity.getEntityId()] ?: 0) / 2
                            if (currentVL <= 0) invalidGround.remove(entity.getEntityId()) else invalidGround.put(entity.getEntityId(), currentVL)
                        }
                        if (entity.isInvisible() && !invisible.contains(entity.getEntityId())) invisible.add(entity.getEntityId())
                    }
                }

                if (event.packet is SPacketAnimation) {
                    val entity = mc.world.getEntityByID(event.packet.entityID)
                    if (entity is EntityLivingBase && event.packet.animationType == 0 && !swing.contains(entity.getEntityId())) swing.add(entity.getEntityId())
                }
            }
        }

        onTick {
            if (!remove) {
                return@onTick
            }
            // Loop through entity list
            // Loop through entity list
            for (o in mc.world.getLoadedEntityList()) {
                if (o is EntityPlayer) {
                    //Make sure it's not the local player + they are in a worrying distance. Ignore them if they're already invalid.
                    if (o != mc.player) {
                        if (!isBot(o)) {
                            continue
                        }
                        mc.world.removeEntity(o)
                    }
                }
            }
        }
    }

    fun isBot(entity: EntityLivingBase): Boolean {
        // Check if entity is a player
        if (entity !is EntityPlayer) return false

        // Check if anti bot is enabled
        if (!isEnabled) return false

        // Anti Bot checks
        if (colorValue && !entity.displayName.formattedText.replace("§r", "").contains("§")) return true
        if (livingTimeValue && entity.ticksExisted < livingTimeTicksValue) return true
        if (groundValue && !ground.contains(entity.entityId)) return true
        if (airValue && !air.contains(entity.entityId)) return true
        if (swingValue && !swing.contains(entity.entityId)) return true
        if (healthValue && entity.health > 20F) return true
        if (entityIDValue && (entity.entityId >= 1000000000 || entity.entityId <= -1)) return true
        if (derpValue && (entity.rotationPitch > 90F || entity.rotationPitch < -90F)) return true
        if (wasInvisibleValue && invisible.contains(entity.entityId)) return true
        if (armorValue) {
            if (entity.inventory.armorInventory[0].isEmpty && entity.inventory.armorInventory[1].isEmpty && entity.inventory.armorInventory[2].isEmpty && entity.inventory.armorInventory[3].isEmpty) return true
        }
        if (pingValue) {
            if (mc.connection!!.getPlayerInfo(entity.uniqueID).responseTime == 0) return true
        }
        if (needHitValue && !hitted.contains(entity.entityId)) return true
        if (invalidGroundValue && invalidGround.getOrDefault(entity.entityId, 0) >= 10) return true
        if (tabValue) {
            val equals: Boolean = tabModeValue == TabMode.Equals
            val targetName: String = stripColor(entity.getDisplayName().formattedText)
            for (networkPlayerInfo in mc.connection!!.playerInfoMap) {
                val networkName: String = stripColor(getName(networkPlayerInfo))
                if (if (equals) targetName == networkName else targetName.contains(networkName)) return false
            }
            return true
        }
        if (duplicateInWorldValue && mc.world!!.loadedEntityList.filter { it is EntityPlayer && it.displayNameString == it.displayNameString }.count() > 1) return true

        if (duplicateInTabValue) {
            if (mc.connection!!.playerInfoMap.stream().filter { networkPlayer -> entity.getName() == stripColor(getName(networkPlayer)) }.count() > 1) return true
        }
        return entity.getName().isEmpty() || entity.getName() == mc.player.name
    }


    override fun onDisable() {
        clearAll()
    }

    private fun stripColor(input: String): String {
        return COLOR_PATTERN.matcher(input).replaceAll("")
    }

    private fun getName(networkPlayerInfoIn: NetworkPlayerInfo): String {
        return if (networkPlayerInfoIn.displayName != null) networkPlayerInfoIn.displayName!!.formattedText else ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.playerTeam, networkPlayerInfoIn.gameProfile.name)
    }


    private fun clearAll() {
        hitted.clear()
        swing.clear()
        ground.clear()
        invalidGround.clear()
        invisible.clear()
    }

    enum class TabMode {
        Equals, Contains
    }
}