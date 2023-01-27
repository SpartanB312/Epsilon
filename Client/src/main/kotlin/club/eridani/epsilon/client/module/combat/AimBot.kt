package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.OnUpdateWalkingPlayerEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.FriendManager
import club.eridani.epsilon.client.management.PlayerPacketManager.sendPlayerPacket
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.EntityUtil
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.math.Vec2f
import club.eridani.epsilon.client.util.onTick
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBow
import net.minecraft.util.math.Vec3d
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

object AimBot : Module(name = "AimBot", category = Category.Combat, description = "Make you look at the target") {

    val side by setting("Side", Side.Client)
    private val aimPriority by setting("Priority", Priority.Distance)
    val players by setting("Players", true)
    private val animals by setting("Animals", false)
    private val mobs by setting("Mobs", false)
    val page by setting("Page", Page.Target)

    private val aimBot by setting("AimBot", true) { page == Page.Target }
    val range by setting("Range", 4f, 0f..10f, 0.1f) { page == Page.Target }
    val factor by setting("Factor", 50, 0..100, 1) { page == Page.Target }

    private val bowAimBot by setting("BowAimBot", false) { page == Page.Bow }
    private val bowRange by setting("BowRange", 30f, 0f..200f, 0.1f) { page == Page.Bow }
    private val predict by setting("Predict", true) { page == Page.Bow }

    private var rangeAimVelocity = 0f
    var target: EntityLivingBase? = null
    var rotation = Vec2f(0f, 0f)

    init {
        onTick {
            target = null
            val entities = mutableListOf<Entity>()
            mc.world.loadedEntityList.filter { isValidEntity(it) && it is EntityLivingBase && !it.isDead && it.health > 0 && !FriendManager.isFriend(it)}.forEach { entity ->
                entities.add(entity)
            }
            entities.sortWith(Comparator.comparing(this::priority).reversed())

            for (entity in entities) {
                if (entity == mc.player || entity !is EntityLivingBase) {
                    continue
                }
                if (mc.player.heldItemMainhand.item is ItemBow) {
                    if (mc.player.getDistance(entity) > bowRange) continue
                } else {
                    if (mc.player.getDistance(entity) > range) continue
                }
                target = entity
            }
        }

        listener<OnUpdateWalkingPlayerEvent.Pre> {
            if (Utils.nullCheck()) return@listener
            runSafe {
                rotation = Vec2f.ZERO

                if (target != null) {
                    if (bowAimBot && mc.player.itemInUseCount > 0 && (mc.player.heldItemMainhand.item is ItemBow || mc.player.heldItemOffhand.item is ItemBow)) {
                        rotation = getBowAim(target, predict)
                    } else if (aimBot) {
                        val rotations = club.eridani.epsilon.client.util.RotationUtil.getRotationsGucel(target)
                        rotation = Vec2f(rotations[0], rotations[1])
                    }

                    if (rotation != Vec2f.ZERO) {
                        when (side) {
                            Side.Client -> {
                                mc.player.rotationYaw = rotation.x
                                mc.player.rotationPitch = rotation.y
                            }
                            Side.Server -> {
                                sendPlayerPacket {
                                    rotate(rotation)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getBowAim(target: Entity?, predict: Boolean): Vec2f {
        val vec = getAimPos(target!!, predict)
        val posSqrt = sqrt(vec.x * vec.x + vec.z * vec.z)
        val rangeCharge = mc.player.itemInUseCount
        rangeAimVelocity = rangeCharge / 20f
        rangeAimVelocity = (rangeAimVelocity * rangeAimVelocity + rangeAimVelocity * 2) / 3
        rangeAimVelocity = 1f
        if (rangeAimVelocity > 1) rangeAimVelocity = 1f
        val g = 0.006f
        val tmp = (rangeAimVelocity * rangeAimVelocity * rangeAimVelocity * rangeAimVelocity - g * (g * (posSqrt * posSqrt) + 2 * vec.y * (rangeAimVelocity * rangeAimVelocity))).toFloat()
        val pitch = (-Math.toDegrees(atan((rangeAimVelocity * rangeAimVelocity - sqrt(tmp.toDouble())) / (g * posSqrt)))).toFloat()
        return Vec2f((atan2(vec.z, vec.x) * 180 / Math.PI).toFloat() - 90f, pitch)
    }

    private fun getAimPos(target: Entity, predict: Boolean): Vec3d {
        val player = mc.player
        return Vec3d(target.posX + (if (predict) (target.posX - target.prevPosX) * 1.35 else 0.0) - (player.posX + if (predict) player.posX - player.prevPosX else 0.0),
            (target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * 1.35 else 0.0) + target.eyeHeight) - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.0) - player.getEyeHeight(),
            target.posZ + (if (predict) (target.posZ - target.prevPosZ) * 1.35 else 0.0) - (player.posZ + if (predict) player.posZ - player.prevPosZ else 0.0))
    }

    fun priority(e: Entity): Float {
        return when (aimPriority) {
            Priority.Distance -> mc.player.getDistance(e)
            Priority.Health -> ((e as EntityLivingBase).health + e.absorptionAmount)
        }
    }

    private fun isValidEntity(entity: Entity): Boolean {
        return entity is EntityLivingBase && players && entity is EntityPlayer || if (EntityUtil.isPassive(entity)) animals else mobs
    }


    enum class Page {
        Target, Bow
    }

    enum class Priority {
        Distance, Health
    }

    enum class Side {
        Client, Server
    }

}