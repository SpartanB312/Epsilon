package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.common.extensions.canBeClicked
import club.eridani.epsilon.client.common.extensions.rightClickDelay
import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.management.FriendManager
import club.eridani.epsilon.client.module.combat.TargetStrafe
import club.eridani.epsilon.client.util.Wrapper.mc
import club.eridani.epsilon.client.util.items.id
import club.eridani.epsilon.client.util.math.MathUtils.getInterpolatedAmount
import club.eridani.epsilon.client.util.math.fastFloor
import club.eridani.epsilon.client.util.math.vector.toBlockPos
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


val Minecraft.isPlayerMovingLegit get() = this.gameSettings.keyBindForward.isKeyDown


val Minecraft.isPlayerMovingKeybind
    get() = this.gameSettings.keyBindForward.isKeyDown || this.gameSettings.keyBindBack.isKeyDown || this.gameSettings.keyBindLeft.isKeyDown || this.gameSettings.keyBindRight.isKeyDown

val EntityLivingBase.relativeHealth get() = this.health + this.absorptionAmount

val Entity.lastTickPos get() = Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ)

val Entity.isPassive
    get() = this is EntityAnimal
            || this is EntityAgeable
            || this is EntityTameable
            || this is EntityAmbientCreature
            || this is EntitySquid

fun SafeClientEvent.getDroppedItem(itemId: Int, range: Float) =
    getDroppedItems(itemId, range)
        .minByOrNull { player.getDistance(it) }
        ?.positionVector
        ?.toBlockPos()

fun SafeClientEvent.getDroppedItems(itemId: Int, range: Float): ArrayList<EntityItem> {
    val entityList = ArrayList<EntityItem>()
    for (entity in world.loadedEntityList) {
        if (entity !is EntityItem) continue /* Entites that are dropped item */
        if (entity.item.item.id != itemId) continue /* Dropped items that are has give item id */
        if (entity.getDistance(player) > range) continue /* Entities within specified  blocks radius */

        entityList.add(entity)
    }
    return entityList
}

val Entity.isNeutral get() = isNeutralMob(this) && !isMobAggressive(this)

val Entity.isHostile get() = isMobAggressive(this)

val Entity.isInOrAboveLiquid get() = this.isInWater || this.isInLava || world.containsAnyLiquid(entityBoundingBox.expand(0.0, -1.0, 0.0))

val EntityPlayer.isFriend get() = FriendManager.isFriend(this)

val EntityPlayer.isFakeOrSelf get() = this == mc.player || this == mc.renderViewEntity || this.entityId < 0

val EntityPlayer.isFlying: Boolean
    get() = this.isElytraFlying || this.capabilities.isFlying

inline fun EntityPlayerSP.spoofSneak(block: () -> Unit) {
//        contract {
//            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//        }

    if (!this.isSneaking) {
        connection.sendPacket(CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING))
        block.invoke()
        connection.sendPacket(CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING))
    } else {
        block.invoke()
    }
}

val Entity.flooredPosition get() = BlockPos(this.posX.fastFloor(), this.posY.fastFloor(), this.posZ.fastFloor())

inline fun EntityPlayerSP.spoofUnSneak(block: () -> Unit) {
//        contract {
//            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
//        }

    if (this.isSneaking) {
        connection.sendPacket(CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING))
        block.invoke()
        connection.sendPacket(CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING))
    } else {
        block.invoke()
    }
}

private fun isNeutralMob(entity: Entity) = entity is EntityPigZombie
        || entity is EntityWolf
        || entity is EntityEnderman
        || entity is EntityIronGolem

private fun isMobAggressive(entity: Entity) = when (entity) {
    is EntityPigZombie -> {
        // arms raised = aggressive, angry = either game or we have set the anger cooldown
        entity.isArmsRaised || entity.isAngry
    }
    is EntityWolf -> {
        entity.isAngry && mc.player != entity.owner
    }
    is EntityEnderman -> {
        entity.isScreaming
    }
    is EntityIronGolem -> {
        entity.revengeTarget != null
    }
    else -> {
        entity.isCreatureType(EnumCreatureType.MONSTER, false)
    }
}

val Entity.isTamed
    get() = this is EntityTameable && this.isTamed || this is AbstractHorse && this.isTame

object EntityUtil : Helper {

    fun getInterpolatedPos(entity: Entity, ticks: Float): Vec3d = entity.lastTickPos.add(getInterpolatedAmount(entity, ticks))


    fun placeBlockScaffold(pos: BlockPos) {
        placeBlockScaffold(pos, true, true, false)
    }

    fun placeBlockScaffold(pos: BlockPos, rotate: Boolean, sneak: Boolean, fast: Boolean) {
        val eyesPos = Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ)
        for (side in EnumFacing.values()) {
            val neighbor = pos.offset(side)
            val side2 = side.opposite

            // check if neighbor can be right clicked
            if (!neighbor.canBeClicked) {
                continue
            }
            val hitVec = Vec3d(neighbor).add(0.5, 0.5, 0.5).add(Vec3d(side2.directionVec).scale(0.5))

            // check if hitVec is within range (4.25 blocks)
            if (eyesPos.squareDistanceTo(hitVec) > 18.0625) {
                continue
            }

            // place block
            if (rotate) RotationUtil.faceVectorPacketInstant(hitVec)
            if (sneak) mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            mc.playerController.processRightClickBlock(mc.player, mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND)
            mc.player.swingArm(EnumHand.MAIN_HAND)
            if (sneak) mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            mc.rightClickDelay = if (fast) 0 else 4
            return
        }
    }


    fun damagePlayer(makeSetPosition: Boolean, damage: Double) {
        if (makeSetPosition) {
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true))
            for (j in 0..getPotionDamageLevel(damage).roundToInt()) {
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625, mc.player.posZ, false))
                mc.player.setPosition(mc.player.posX, mc.player.posY + 0.0625, mc.player.posZ)
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05510000046342611, mc.player.posZ, false))
                mc.player.setPosition(mc.player.posX, mc.player.posY + 0.05510000046342611, mc.player.posZ)
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.3579E-6, mc.player.posZ, false))
                mc.player.setPosition(mc.player.posX, mc.player.posY + 1.3579E-6,mc.player.posZ)
            }
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true))
        } else {
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true))
            for (j in 0..getPotionDamageLevel(damage).roundToInt()) {
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625, mc.player.posZ, false))
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.05510000046342611, mc.player.posZ, false))
                mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.3579E-6, mc.player.posZ, false))
            }
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true))
        }
    }

    private fun getPotionDamageLevel(damage: Double): Double {
        return if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            48 + (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST))!!.amplifier + 1) * 16 + (damage - 1.0) * 16.0
        } else 48.0
    }

    fun getHealthColor(entity: EntityPlayer, alpha: Int): ColorRGB {
        val delta =
            Color.HSBtoRGB(0.0f.coerceAtLeast(entity.health.coerceAtMost(entity.maxHealth) / entity.maxHealth) / 3.0f, 1.0f, 1.0f) or -0x1000000
        val color = Color(delta)
        return ColorRGB(color.red, color.green, color.blue, alpha)
    }

    fun isPassive(e: Entity?): Boolean {
        if (e is EntityWolf && e.isAngry) {
            return false
        }
        return if (e is EntityAgeable || e is EntityAmbientCreature || e is EntitySquid) {
            true
        } else e is EntityIronGolem && e.revengeTarget == null
    }

    fun centerPlayer(centerPos: BlockPos) {
        val playerPos = mc.player.positionVector
        val y = centerPos.y.toDouble()
        var x = centerPos.x.toDouble()
        var z = centerPos.z.toDouble()
        val plusPlus = Vec3d(x + 0.5, y, z + 0.5)
        val plusMinus = Vec3d(x + 0.5, y, z - 0.5)
        val minusMinus = Vec3d(x - 0.5, y, z - 0.5)
        val minusPlus = Vec3d(x - 0.5, y, z + 0.5)

        if (playerPos.distanceTo(plusPlus) < playerPos.distanceTo(plusMinus) && playerPos.distanceTo(plusPlus) < playerPos.distanceTo(minusMinus) && playerPos.distanceTo(plusPlus) < playerPos.distanceTo(minusPlus)) {
            x = centerPos.x.toDouble() + 0.5
            z = centerPos.z.toDouble() + 0.5
            centerPlayer(x, y, z)
        }
        if (playerPos.distanceTo(plusMinus) < playerPos.distanceTo(plusPlus) && playerPos.distanceTo(plusMinus) < playerPos.distanceTo(minusMinus) && playerPos.distanceTo(plusMinus) < playerPos.distanceTo(minusPlus)) {
            x = centerPos.x.toDouble() + 0.5
            z = centerPos.z.toDouble() - 0.5
            centerPlayer(x, y, z)
        }
        if (playerPos.distanceTo(minusMinus) < playerPos.distanceTo(plusPlus) && playerPos.distanceTo(minusMinus) < playerPos.distanceTo(plusMinus) && playerPos.distanceTo(minusMinus) < playerPos.distanceTo(minusPlus)) {
            x = centerPos.x.toDouble() - 0.5
            z = centerPos.z.toDouble() - 0.5
            centerPlayer(x, y, z)
        }
        if (playerPos.distanceTo(minusPlus) < playerPos.distanceTo(plusPlus) && playerPos.distanceTo(minusPlus) < playerPos.distanceTo(plusMinus) && playerPos.distanceTo(minusPlus) < playerPos.distanceTo(minusMinus)) {
            x = centerPos.x.toDouble() - 0.5
            z = centerPos.z.toDouble() + 0.5
            centerPlayer(x, y, z)
        }
    }

    fun setMotion(event: PlayerMoveEvent.Pre, speed: Double) {
        var forward = mc.player.movementInput.moveForward.toDouble()
        var strafe = mc.player.movementInput.moveStrafe.toDouble()
        var yaw = mc.player.rotationYaw

        if (TargetStrafe.isEnabled) {
            forward = TargetStrafe.forward.toDouble()
            strafe = TargetStrafe.direction.toDouble()
            yaw = TargetStrafe.yaw
        }

        if (forward == 0.0 && strafe == 0.0) {
            event.x = 0.0
            event.z = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
            event.x = forward * speed * cos + strafe * speed * sin
            event.z = forward * speed * sin - strafe * speed * cos
        }
    }

    fun setMotion(speed: Double, entity: Entity) {
        var forward = mc.player.movementInput.moveForward.toDouble()
        var strafe = mc.player.movementInput.moveStrafe.toDouble()
        var yaw = mc.player.rotationYaw
        if (forward == 0.0 && strafe == 0.0) {
            entity.motionX = 0.0
            entity.motionZ = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
            entity.motionX = forward * speed * cos + strafe * speed * sin
            entity.motionZ = forward * speed * sin - strafe * speed * cos
        }
    }


    fun isOnGround(height: Double): Boolean {
        return mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, -height, 0.0)).isNotEmpty()
    }
    fun isMoving(e: Entity): Boolean {
        return e.motionX != 0.0 && e.motionZ != 0.0 && (e.motionY != 0.0 || e.motionY > 0.0)
    }
    private fun centerPlayer(x: Double, y: Double, z: Double) {
        mc.player.connection.sendPacket(CPacketPlayer.Position(x, y, z, true))
        mc.player.setPosition(x, y, z)
    }
}