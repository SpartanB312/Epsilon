package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.attackDamage
import club.eridani.epsilon.client.common.extensions.runSafeTask
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.management.FriendManager.isFriend
import club.eridani.epsilon.client.management.PlayerPacketManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.misc.AntiBot
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.math.MathUtils.randomPolarity
import club.eridani.epsilon.client.util.math.Vec2f
import club.eridani.epsilon.client.util.*
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureAttribute
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random


object KillAura :
    Module(name = "KillAura", alias = arrayOf("Aura"), category = Category.Combat, description = "Auto-attack entity you nearby") {
    val range by setting("Range", 4.45, 0.0..30.0, 0.01)
    private val render by setting("Render", true)
    private val renderMode by setting("RenderMode", RenderMode.Jello) { render }
    private val invisible by setting("Invisible", true)
    private val walls by setting("Through Walls", true)
    private val only32k by setting("32kOnly", false)
    private val playersOnly by setting("Players Only", true)
    private val swordOnly by setting("Sword Only", true)
    private val autoSwitch by setting("AutoSwitch", false)
    private val rotate by setting("Rotate", true)
    private val rayCast by setting("RayCast", true) { rotate }
    private val rotateSpeed by setting("Rotate Speed", 50, 1..100, 1) { rotate }
    private val tps by setting("TpsSync", true)
    private val mode by setting("Mode", Mode.Auto)
    private val speed by setting("CPS", 13f, 0f..40f, 0.1f) { mode == Mode.CPS }
    private val randomRotation by setting("Random Rotation", 3.0, 1.0..10.0, 0.1) { mode == Mode.Hypixel }
    private val aps by setting("APS", 13.0, 0.0..20.0, 0.1) { mode == Mode.Hypixel }
    private val randomization by setting("Randomization", 3.0, 1.0..7.0, 0.1) { mode == Mode.Hypixel }
    private val autoBlock by setting("AutoBlock", false) { mode == Mode.Hypixel }
    private val cpt by setting("CPT", 3, 0..40, 1) { mode == Mode.CPT }
    private val delay by setting("MsDelay", 2f, 0f..1000f, 1f) { mode == Mode.MS }
    private val tickDelay by setting("TickDelay", 2, 0..50, 1) { mode == Mode.TickDelay }
    private val priorityMode by setting("Priority", Priority.Distance)


    private var hasWaited = 0
    private val timer = Timer()
    private var yaw = 0f
    private var pitch = 0f
    private var nextRandom = floatArrayOf(0f, 0f)
    var currentTarget: EntityLivingBase? = null

    @JvmStatic
    private val targetList = mutableListOf<EntityLivingBase>()
    private var isSpoofingAngles = false
    private var attackMS = 0L
    private var lastAttackMS = -1L

    init {
        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            runSafeTask {
                if (currentTarget != null && rotate) {
                    lookAtPacket(currentTarget)
                }
            }
        }

        onTick {
            runSafeTask {
                if (mc.player.heldItemMainhand.item !is ItemSword && swordOnly) {
                    currentTarget = null
                    return@onTick
                }

                if (!checkSharpness(mc.player.heldItemMainhand) && only32k) {
                    currentTarget = null
                    return@onTick
                }

                updateTargetList()

                if (targetList.isEmpty()) {
                    currentTarget = null
                    return@onTick
                }

                val target = targetList.minByOrNull { entityLivingBase -> priority(entityLivingBase) }
                currentTarget = target

                if (target == null) {
                    if (isSpoofingAngles) {
                        resetRotation()
                    }
                    return@onTick
                }

                mc.player.setLastAttackedEntity(target)


                //check rotation is directly raytrace to that target
                if (rotate && rayCast && !RotationUtil.rayCast(target, yaw + nextRandom[0], normalPitch(pitch + nextRandom[1]), range)) {
                    return@onTick
                }

                when (mode) {
                    Mode.Auto -> {
                        val ticks = 20 - TpsCalculator.tickRate
                        val canAttack = if (tps) {
                            mc.player.getCooledAttackStrength(0.5f + ticks) >= 1f
                        } else {
                            mc.player.getCooledAttackStrength(0f) >= 1f
                        }
                        if (canAttack) {
                            attack(target)
                        }
                    }
                    Mode.TickDelay -> {
                        if (hasWaited >= tickDelay) {
                            attack(target)
                            hasWaited = 0
                        } else {
                            ++hasWaited
                        }
                    }
                    Mode.MS -> {
                        if (timer.passed((delay * if (tps) 20f / TpsCalculator.tickRate else 1f).roundToInt())) {
                            attack(target)
                            timer.reset()
                        }
                    }
                    Mode.CPS -> {
                        val atkSpeed = (1000 / (speed * if (tps) 20 / TpsCalculator.tickRate else 1.0f)).roundToInt().toLong()
                        var i = 0
                        while (i < 2) {
                            attackMS = System.nanoTime() / 1000000
                            if (hasDelayRun(atkSpeed)) {
                                attack(target)
                                lastAttackMS = System.nanoTime() / 1000000
                            }
                            i++
                        }
                    }
                    Mode.CPT -> {
                        var i = 0
                        while (i < cpt) {
                            attack(target)
                            ++i
                        }
                    }
                    Mode.Hypixel -> {
                        val atkSpeed = (1000 / (aps + (if (Random.nextBoolean()) -1 else 1) * Random.nextDouble(0.0, randomization))).roundToLong()
                        attackMS = System.nanoTime() / 1000000
                        val stack = mc.player.getHeldItem(EnumHand.MAIN_HAND)
                        if (autoBlock && stack.item is ItemSword) {
                            mc.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                        if (hasDelayRun(atkSpeed)) {
                            attack(target)
                            lastAttackMS = System.nanoTime() / 1000000
                        }
                    }
                }
            }
        }

        onRender3D { event ->
            if (render && currentTarget != null) {
                if (renderMode == RenderMode.Circle) RenderUtils3D.drawCircleESP(
                    currentTarget!!, event.partialTicks,
                    TargetStrafe.range, Color.WHITE.rgb)
                else RenderUtils3D.jelloRender(currentTarget!!)
            }
        }
    }


    private fun getArmor(e: EntityPlayer): Double {
        val armorValueRate: Double
        val armorValue: Double = e.totalArmorValue.toDouble()
        armorValueRate = 1 - armorValue * 4 / 100
        return armorValueRate
    }

    private fun calculateDamage(f: Entity): Double {
        val damage: Double
        val outPut: Double
        val currentItemSlot = mc.player.inventory.currentItem
        val onHand = mc.player.inventory.getStackInSlot(currentItemSlot)
        damage = when (onHand.item) {
            is ItemSword -> {
                (onHand.item as ItemSword).attackDamage + EnchantmentHelper.getModifierForCreature(onHand, EnumCreatureAttribute.UNDEFINED).toDouble()
            }
            is ItemAxe -> {
                (onHand.item as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(onHand, EnumCreatureAttribute.UNDEFINED).toDouble()
            }
            is ItemTool -> {
                (onHand.item as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(onHand, EnumCreatureAttribute.UNDEFINED).toDouble()
            }
            else -> {
                1.0
            }
        }
        val targetHealth: Double = ((f as EntityLivingBase).health + f.absorptionAmount).toDouble()
        outPut = if (f is EntityPlayer) {
            targetHealth - damage * getArmor(f)
        } else {
            targetHealth - damage
        }
        return outPut
    }

    private fun attack(e: Entity) {
        if (autoSwitch) {
            equipBestWeapon()
        }
        if (mode == Mode.Hypixel) {
            mc.player.swingArm(EnumHand.MAIN_HAND)
            mc.playerController.attackEntity(mc.player, e)
        } else {
            mc.playerController.attackEntity(mc.player, e)
            mc.player.swingArm(EnumHand.MAIN_HAND)
        }
        mc.player.resetCooldown()
    }

    private fun equipBestWeapon() {
        var bestSlot = -1
        var maxDamage = 0.0
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.isEmpty) continue
            if (stack.item is ItemTool) {
                val damage = (stack.item as ItemTool).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            } else if (stack.item is ItemSword) {
                val damage = (stack.item as ItemSword).attackDamage + EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED).toDouble()
                if (damage > maxDamage) {
                    maxDamage = damage
                    bestSlot = i
                }
            }
        }
        if (bestSlot != -1) ItemUtil.swapToSlot(bestSlot)
    }

    private fun hasDelayRun(time: Long): Boolean {
        return attackMS - lastAttackMS >= time
    }

    private fun updateTargetList() {
        targetList.clear()
        for (entity in mc.world.loadedEntityList) {
            if (entity !is EntityLivingBase) continue
            if (isValid(entity)) {
                targetList.add(entity)
            }
        }
    }

    fun priority(e: Entity): Double {
        return when (priorityMode) {
            Priority.Distance -> mc.player.getDistance(e).toDouble()
            Priority.Damage -> calculateDamage(e)
            Priority.Health -> ((e as EntityLivingBase).relativeHealth).toDouble()
        }
    }

    private fun isValid(entityLivingBase: EntityLivingBase): Boolean {
        if (entityLivingBase == mc.player || entityLivingBase.isDead
            || isFriend(entityLivingBase)
            || mc.player.getDistance(entityLivingBase) > range || entityLivingBase.health == 0f || entityLivingBase is EntityArmorStand || AntiBot.isBot(entityLivingBase)
        ) {
            return false
        }

        if (!walls && !entityLivingBase.canEntityBeSeen(mc.player)) return false
        if (!invisible && entityLivingBase.isInvisible) return false
        if (playersOnly && entityLivingBase !is EntityPlayer) return false
        return true
    }


    private fun lookAtPacket(target: EntityLivingBase?) {
        val v = RotationUtil.getRotationsGucel(target)
        val rotateSpeed = rotateSpeed - 1 + Random.nextDouble()
        val smooth = RotationUtil.faceEntitySmooth(yaw.toDouble(), pitch.toDouble(), v[0].toDouble(), v[1].toDouble(), rotateSpeed, rotateSpeed)
        setYawAndPitch(smooth[0], smooth[1])
    }

    private fun setYawAndPitch(yaw1: Float, pitch1: Float) {
        yaw = yaw1//RotationUtil.updateRotation(PlayerPacketManager.prevRotation.x, yaw1, 360.0f)
        pitch = pitch1

        if (pitch > 90.0f) pitch = 90.0f
        else if (pitch < -90.0f) pitch = -90.0f

        PlayerPacketManager.sendPacket(800) {
            rotate(Vec2f(yaw + nextRandom[0], normalPitch(pitch + nextRandom[1])))
        }

        nextRandom = if (mode == Mode.Hypixel) {
            floatArrayOf(Random.nextDouble(randomRotation).toFloat().randomPolarity(), Random.nextDouble(randomRotation).toFloat().randomPolarity())
        } else {
            floatArrayOf(0f, 0f)
        }

        PlayerPacketManager.rotation
        isSpoofingAngles = true
    }

    private fun normalPitch(pitch: Float): Float {
        return pitch.coerceAtLeast(-90f).coerceAtMost(90f)
    }

    private fun resetRotation() {
        yaw = mc.player.rotationYaw
        pitch = mc.player.rotationPitch
        isSpoofingAngles = false
    }

    @JvmStatic
    fun checkSharpness(stack: ItemStack): Boolean {
        if (stack.tagCompound == null || stack.item == null) {
            return false
        }
        val tagCompound = stack.tagCompound
        val attributes = tagCompound!!.getTag("AttributeModifiers")
        if (attributes is NBTTagList) {
            for (i in 0 until attributes.tagCount()) {
                val attribute = attributes.getCompoundTagAt(i)
                if (attribute.getString("AttributeName") != SharedMonsterAttributes.ATTACK_DAMAGE.name) continue
                if (attribute.getInteger("Amount") >= 114.514f) return true
            }
        }
        val enchants: NBTTagList? = tagCompound.getTag("ench") as NBTTagList?
        if (enchants != null) for (i in 0 until enchants.tagCount()) {
            val enchant = enchants.getCompoundTagAt(i)
            if (enchant.getInteger("id") != 16) continue
            val lvl = enchant.getInteger("lvl")
            if (lvl < 42) break
            return true
        }
        return false
    }

    override fun getHudInfo(): String {
        return mode.name
    }

    enum class Mode {
        Auto, TickDelay, MS, CPS, CPT, Hypixel
    }

    enum class RenderMode {
        Jello, Circle
    }

    enum class Priority {
        Distance, Damage, Health
    }

}