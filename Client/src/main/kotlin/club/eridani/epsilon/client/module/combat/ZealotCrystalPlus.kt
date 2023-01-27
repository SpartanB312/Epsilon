package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.collections.CircularArray
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.event.safeParallelListener
import club.eridani.epsilon.client.management.HotbarManager.serverSideItem
import club.eridani.epsilon.client.management.HotbarManager.spoofHotbar
import club.eridani.epsilon.client.management.PlayerPacketManager.sendPlayerPacket
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.setting.GuiSetting
import club.eridani.epsilon.client.util.combat.CrystalUtils
import club.eridani.epsilon.client.util.combat.CrystalUtils.canPlaceCrystalOn
import club.eridani.epsilon.client.util.combat.CrystalUtils.isResistant
import club.eridani.epsilon.client.util.delegate.CachedValueN
import club.eridani.epsilon.client.util.extension.AxisAlignedBB.interp
import club.eridani.epsilon.client.util.graphics.Easing
import club.eridani.epsilon.client.util.graphics.ProjectionUtils
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.inventory.operation.swapToSlot
import club.eridani.epsilon.client.util.math.RotationUtils.getRotationTo
import club.eridani.epsilon.client.util.math.VectorUtils.setAndAdd
import club.eridani.epsilon.client.util.math.VectorUtils.toViewVec
import club.eridani.epsilon.client.util.threads.runSafe
import club.eridani.epsilon.client.util.world.FastRayTraceAction
import club.eridani.epsilon.client.util.world.fastRaytrace
import club.eridani.epsilon.client.util.world.isAir
import club.eridani.epsilon.client.util.world.rayTraceVisible
import club.eridani.epsilon.client.common.extensions.*
import club.eridani.epsilon.client.event.events.CrystalSetDeadEvent
import club.eridani.epsilon.client.event.events.RenderEntityEvent
import club.eridani.epsilon.client.event.events.TickEvent
import club.eridani.epsilon.client.management.*
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.extension.*
import club.eridani.epsilon.client.util.inventory.slot.*
import club.eridani.epsilon.client.util.math.*
import club.eridani.epsilon.client.util.math.vector.distanceSq
import club.eridani.epsilon.client.util.math.vector.distanceSqTo
import club.eridani.epsilon.client.util.math.vector.distanceTo
import club.eridani.epsilon.client.util.math.vector.toVec3dCenter
import it.unimi.dsi.fastutil.ints.Int2LongMaps
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import net.minecraft.block.state.IBlockState
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.CombatRules
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.EnumDifficulty
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
object ZealotCrystalPlus :
    Module(name = "ZealotCrystal+", description = "Lol wtf", category = Category.Combat, priority = 2000) {
    private val page by setting("Page", Page.General)

    // General
    private val players by setting("Players", true) { page == Page.General }
    private val mobs by setting("Mobs", false) { page == Page.General }
    private val animals by setting("Animals", false) { page == Page.General }
    private val maxTargets by setting("Max Targets", 4, 1..10, 1) { page == Page.General }
    private val targetRange by setting("Target Range", 12.0f, 0.0f..24.0f, 0.5f) { page == Page.General }
    private val rotation by setting("Rotation", true) { page == Page.General }
    private val yawSpeed by setting("Yaw Speed", 45.0f, 5.0f..180.0f, 5.0f) { page == Page.General && rotation }
    private val placeRotationRange by setting("Place Rotation Range", 0.0f, 0.0f..180.0f, 5.0f) { page == Page.General && rotation }
    private val breakRotationRange by setting("Break Rotation Range", 90.0f, 0.0f..180.0f, 5.0f) { page == Page.General && rotation }
    private val eatingPause by setting("Eating Pause", false) { page == Page.General }
    private val miningPause by setting("Mining Pause", false) { page == Page.General }

    // Force Place
    private val forcePlaceHealth by setting("Force Place Health", 8.0f, 0.0f..20.0f, 0.5f) { page == Page.ForcePlace }
    private val forcePlaceArmorRate by setting("Force Place Armor Rate", 3, 0..25, 1) { page == Page.ForcePlace }
    private val forcePlaceMinDamage by setting("Force Place Min Damage", 1.5f, 0.0f..10.0f, 0.25f) { page == Page.ForcePlace }
    private val forcePlaceMotion by setting("Force Place Motion", 4.0f, 0.0f..10.0f, 0.25f) { page == Page.ForcePlace }
    private val forcePlaceBalance by setting("Force Place Balance", -1.0f, -10.0f..10.0f, 0.25f) { page == Page.ForcePlace }
    private val forcePlaceSword by setting("Force Place Sword", false) { page == Page.ForcePlace }

    // Calculation
    private val noSuicide by setting("No Suicide", 2.0f, 0.0f..20.0f, 0.25f) { page == Page.Calculation }
    private val wallRange by setting("Wall Range", 3.0f, 0.0f..8.0f, 0.1f) { page == Page.Calculation }
    private val forceUpdate by setting("Force Update", true) { page == Page.Calculation }
    private val updateDelay by setting("Update Delay", 25, 0..250, 1) { page == Page.Calculation }
    private val motionPredict by setting("Motion Predict", true) { page == Page.Calculation }
    private val predictTicks by setting("Predict Ticks", 8, 0..20, 1) { page == Page.Calculation && motionPredict }
    private val breakCalculation by setting("Break Calculation", BreakCalculation.Multi) { page == Page.Calculation }
    private val lethalOverride by setting("Lethal Override", true) { page == Page.Calculation }
    private val lethalBalance by setting("Lethal Balance", 0.5f, -5.0f..5.0f, 0.1f) { page == Page.Calculation && lethalOverride }
    private val lethalMaxDamage by setting("Lethal Max Damage", 16.0f, 0.0f..20.0f, 0.25f) { page == Page.Calculation && lethalOverride }
    private val safeOverride by setting("Safe Override", true) { page == Page.Calculation }
    private val safeRange by setting("Safe Range", 0.5f, 0.0f..5.0f, 0.1f) { page == Page.Calculation && safeOverride }
    private val safeThreshold by setting("Safe Threshold", 2.0f, 0.0f..5.0f, 0.1f) { page == Page.Calculation && safeOverride }

    // Place
    private val placeMode by setting("Place Mode", PlaceMode.Single) { page == Page.Place }
    private val packetPlace by setting("Packet Place", PacketPlaceMode.Weak) { page == Page.Place }
    private val spamPlace by setting("Spam Place", true) { page == Page.Place }
    private val autoSwap by setting("Auto Swap", SwapMode.Off) { page == Page.Place }
    private val newPlacement by setting("1.13 Place", false) { page == Page.Place }
    private val placeSwing by setting("Place Swing", false) { page == Page.Place }
    private val placeBypass by setting("Place Bypass", PlaceBypass.Up) { page == Page.Place }
    private val placeMinDamage by setting("Place Min Damage", 5.0f, 0.0f..20.0f, 0.25f) { page == Page.Place }
    private val placeMaxSelfDamage by setting("Place Max Self Damage", 6.0f, 0.0f..20.0f, 0.25f) { page == Page.Place }
    private val placeBalance by setting("Place Balance", -3.0f, -10.0f..10.0f, 0.25f) { page == Page.Place }
    private val placeDelay by setting("Place Delay", 50, 0..500, 1) { page == Page.Place }
    private val placeRange by setting("Place Range", 5.0f, 0.0f..8.0f, 0.1f) { page == Page.Place }

    // Break
    private val breakMode by setting("Break Mode", BreakMode.Smart) { page == Page.Break }
    private val bbtt by setting("2B2T", false) { page == Page.Break }
    private val bbttFactor by setting("2B2T Factor", 200, 0..1000, 25) { page == Page.Break && bbtt }
    private val packetBreak by setting("Packet Break", BreakMode.Target) { page == Page.Break && !bbtt }
    private val ownTimeout by setting("Own Timeout", 100, 0..2000, 25) { page == Page.Break && (breakMode == BreakMode.Own || packetBreak == BreakMode.Own) }
    private val antiWeakness by setting("Anti Weakness", SwapMode.Off) { page == Page.Break }
    private val swapDelay by setting("Swap Delay", 0, 0..20, 1) { page == Page.Break }
    private val breakMinDamage by setting("Break Min Damage", 4.0f, 0.0f..20.0f, 0.25f) { page == Page.Break }
    private val breakMaxSelfDamage by setting("Break Max Self Damage", 8.0f, 0.0f..20.0f, 0.25f) { page == Page.Break }
    private val breakBalance by setting("Break Balance", -4.0f, -10.0f..10.0f, 0.25f) { page == Page.Break }
    private val breakDelay by setting("Break Delay", 100, 0..500, 1) { page == Page.Break }
    private val breakRange by setting("Break Range", 5.0f, 0.0f..8.0f, 0.1f) { page == Page.Break }

    // Misc
    private val swingMode by setting("Swing Mode", SwingMode.Client) { page == Page.Render }
    private val swingHand by setting("Swing Hand", SwingHand.Auto) { page == Page.Render }
    private val filledAlpha by setting("Filled Alpha", 63, 0..255, 1) { page == Page.Render }
    private val outlineAlpha by setting("Outline Alpha", 200, 0..255, 1) { page == Page.Render }
    private val targetDamage by setting("Target Damage", true) { page == Page.Render }
    private val selfDamage by setting("Self Damage", true) { page == Page.Render }
    private val targetChams by setting("Target Chams", RenderMode.Single) { page == Page.Render }
    private val chamsAlpha by setting("Chams Alpha", 64, 0..255, 1) { page == Page.Render && targetChams != RenderMode.Off }
    private val renderTarget by setting("Render Target", true) { page == Page.Render }
    private val rainbowColor by setting("Render Color", false) { page == Page.Render }
    private val hudInfo by setting("Hud Info", HudInfo.Speed) { page == Page.Render }
    private val movingLength by setting("Moving Length", 400, 0..1000, 50) { page == Page.Render }
    private val fadeLength by setting("Fade Length", 200, 0..1000, 50) { page == Page.Render }

    private enum class Page {
        General, ForcePlace, Calculation, Place, Break, Render
    }

    @Suppress("unused")
    private enum class SwingMode {
        Client {
            override fun swingHand(event: SafeClientEvent, hand: EnumHand) {
                event.player.swingArm(hand)
            }
        },
        Packet {
            override fun swingHand(event: SafeClientEvent, hand: EnumHand) {
                event.connection.sendPacket(CPacketAnimation(hand))
            }
        };

        abstract fun swingHand(event: SafeClientEvent, hand: EnumHand)
    }

    private enum class SwingHand {
        Auto, OffHand, MainHand
    }

    private enum class BreakCalculation {
        Single, Multi
    }

    private enum class SwapMode {
        Off, Swap, Spoof
    }

    @Suppress("unused")
    private enum class PlaceMode {
        Off, Single, Multi
    }

    @Suppress("unused")
    private enum class PacketPlaceMode(val onRemove: Boolean, val onBreak: Boolean) {
        Off(false, false), Weak(true, false), Strong(true, true)
    }

    private enum class PlaceBypass {
        Up, Down, Closest
    }

    private enum class BreakMode {
        Off, Target, Own, Smart, All,
    }

    private enum class RenderMode {
        Off, Single, Multi
    }

    private enum class HudInfo {
        Off, Speed, Target, Damage
    }

    private val renderTargetSet = CachedValueN(5L) {
        IntOpenHashSet().apply {
            targets.getLazy()?.forEach {
                add(it.entity.entityId)
            }
        }
    }

    private val targets = CachedValueN(25L) {
        runSafe {
            getTargets()
        } ?: emptySequence()
    }

    private val rotationInfo = CachedValueN(25L, PlaceInfo.INVALID) {
        runSafe {
            calcPlaceInfo(false)
        }
    }

    private val placeInfo = CachedValueN(25L, PlaceInfo.INVALID) {
        runSafe {
            calcPlaceInfo(rotation)
        }
    }

    private val renderPlaceInfo: PlaceInfo?
        get() = if (rotation) rotationInfo.getLazy() else placeInfo.getLazy()

    @JvmStatic
    val target: EntityLivingBase?
        get() = placeInfo.getLazy()?.target

    private val placedPosMap = Long2LongMaps.synchronize(Long2LongOpenHashMap())
    private val crystalSpawnMap = Int2LongMaps.synchronize(Int2LongOpenHashMap())
    private val attackedCrystalMap = Int2LongMaps.synchronize(Int2LongOpenHashMap())

    private val timeoutTimer = TickTimer()
    private val placeTimer = TickTimer()
    private val breakTimer = TickTimer()

    private var lastActiveTime = 0L
    private var lastRotation: PlaceInfo? = null

    private val explosionTimer = TickTimer()
    private val explosionCountArray = CircularArray<Int>(8)
    private var explosionCount = 0

    private val updateThread = Thread({
        while (true) {
            try {
                while (isDisabled) {
                    try {
                        Thread.sleep(1000L)
                    } catch (e: InterruptedException) {
                        break
                    }
                }

                targets.get()
                if (rotation) rotationInfo.get(mc.timer.tickLength.toInt())
                placeInfo.get(updateDelay)

                if (explosionTimer.tickAndReset(250L)) {
                    val count = explosionCount
                    explosionCount = 0
                    explosionCountArray.add(count)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Thread.sleep(1L)
        }
    }, "Zealot+ Update").apply {
        isDaemon = true
        start()
    }

    private val loopThread = Thread({
        while (true) {
            try {
                while (isDisabled) {
                    try {
                        Thread.sleep(1000L)
                    } catch (e: InterruptedException) {
                        break
                    }
                }

                runLoop()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            Thread.sleep(1L)
        }
    }, "Zealot+ Loop").apply {
        isDaemon = true
        start()
    }

    override fun onEnable() {
        updateThread.interrupt()
        loopThread.interrupt()
    }

    override fun onDisable() {
        placeTimer.reset(-114514L)
        breakTimer.reset(-114514L)

        lastActiveTime = 0L
        lastRotation = null

        explosionTimer.reset(-114514L)
        explosionCountArray.clear()
        explosionCount = 0

        Renderer.reset()
    }


    override fun getHudInfo(): String {
        return when (hudInfo) {
            HudInfo.Off -> ""
            HudInfo.Speed -> "%.1f".format(explosionCountArray.average() * 4.0)
            HudInfo.Damage -> renderPlaceInfo?.let { "%.1f/%.1f".format(it.targetDamage, it.selfDamage) } ?: "0.0/0.0"
            HudInfo.Target -> target?.name ?: "None"
        }
    }

    init {
        onRender3D {
            Renderer.onRender3D()
        }

        onRender2D {
            Renderer.onRender2D()
        }

        safeListener<RenderEntityEvent.Model.Pre> {
            if (!it.cancelled && isValidEntityForRendering(targetChams, it.entity)) {
                GL11.glDepthRange(0.0, 0.01)
                GuiSetting.firstGuiColor.value.alpha(chamsAlpha).setGLColor()
                GlStateManager.disableTexture2D()
                GlStateManager.disableLighting()
                GlStateManager.enableBlend()
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            }
        }

        safeListener<RenderEntityEvent.Model.Post> {
            if (!it.cancelled && isValidEntityForRendering(targetChams, it.entity)) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                GlStateManager.enableTexture2D()
                GlStateManager.enableLighting()
            }
        }

        safeListener<RenderEntityEvent.All.Post> {
            if (!it.cancelled && isValidEntityForRendering(targetChams, it.entity)) {
                GL11.glDepthRange(0.0, 1.0)
            }
        }

        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            runSafe {
                if (paused()) return@runSafe

                if (!rotation) return@runSafe

                var placing = System.currentTimeMillis() - lastActiveTime <= 250L
                rotationInfo.get(ZealotCrystalPlus.mc.timer.tickLength.toInt() * 2)?.let {
                    lastRotation = it
                    placing = true
                }

                if (placing) {
                    lastRotation?.let {
                        val rotation = getRotationTo(it.hitVec)
                        val diff = RotationUtils.calcAngleDiff(rotation.x, PlayerPacketManager.rotation.x)

                        if (abs(diff) <= yawSpeed) {
                            sendPlayerPacket {
                                rotate(rotation)
                            }
                        } else {
                            val clamped = diff.coerceIn(-yawSpeed, yawSpeed)
                            val newYaw = RotationUtils.normalizeAngle(PlayerPacketManager.rotation.x + clamped)

                            sendPlayerPacket {
                                rotate(Vec2f(newYaw, rotation.y))
                            }
                        }
                    }
                } else {
                    lastRotation = null
                }
            }
        }

        onPacketReceive {
            runSafe {
                if (paused()) return@runSafe

                if (it.packet is SPacketSpawnObject) {
                    handleSpawnObject(it.packet)
                }
            }
        }

        safeListener<CrystalSetDeadEvent> {
            if (paused()) return@safeListener

            handlePlacedExplosion(it.x, it.y, it.z, it.crystals)
            placedPosMap.clear()
            crystalSpawnMap.clear()
            attackedCrystalMap.clear()
        }

        safeParallelListener<TickEvent> {
            for (entity in world.loadedEntityList) {
                if (entity !is EntityLivingBase) continue
                reductionMap[entity] = DamageReduction(entity)
            }
        }
    }

    private /*inline*/ fun isValidEntityForRendering(renderMode: RenderMode, entity: Entity): Boolean {
        return when (renderMode) {
            RenderMode.Off -> false
            RenderMode.Single -> entity == (target ?: targets.getLazy()?.firstOrNull())
            RenderMode.Multi -> renderTargetSet.get().contains(entity.entityId)
        }
    }

    private /*inline*/ fun drawEntityPrediction(buffer: BufferBuilder, entity: Entity, motion: Vec3d, partialTicks: Float) {
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks

        val endX = x + motion.x
        val endY = y + motion.y
        val endZ = z + motion.z

        buffer.pos(x, y, z).endVertex()
        buffer.pos(endX, endY, endZ).endVertex()
        buffer.pos(endX, endY, endZ).endVertex()
        buffer.pos(endX, endY + entity.eyeHeight, endZ).endVertex()
    }

    private /*inline*/ fun SafeClientEvent.handleSpawnObject(packet: SPacketSpawnObject) {
        val mutableBlockPos = BlockPos.MutableBlockPos()
        if (packet.type == 51) {
            if (checkBreakRange(packet.x, packet.y, packet.z, mutableBlockPos)) {
                crystalSpawnMap[packet.entityID] = System.currentTimeMillis()

                if (!bbtt && checkCrystalRotation(packet.x, packet.y, packet.z)) {
                    placeInfo.getLazy()?.let {
                        when (packetBreak) {
                            BreakMode.Target -> {
                                if (CrystalUtils.placeBoxIntersectsCrystalBox(packet.x, packet.y, packet.z, it.blockPos)) {
                                    breakDirect(packet.x, packet.y, packet.z, packet.entityID)
                                }
                            }
                            BreakMode.Own -> {
                                if (CrystalUtils.placeBoxIntersectsCrystalBox(packet.x, packet.y, packet.z, it.blockPos) || placedPosMap.containsKey(
                                        toLong(packet.x, packet.y - 1.0, packet.z)
                                    ) && checkBreakDamage(packet.x, packet.y, packet.z, mutableBlockPos)) {
                                    breakDirect(packet.x, packet.y, packet.z, packet.entityID)
                                }
                            }
                            BreakMode.Smart -> {
                                if (CrystalUtils.placeBoxIntersectsCrystalBox(packet.x, packet.y, packet.z, it.blockPos) || checkBreakDamage(packet.x, packet.y, packet.z, mutableBlockPos)) {
                                    breakDirect(packet.x, packet.y, packet.z, packet.entityID)
                                }
                            }
                            BreakMode.All -> {
                                breakDirect(packet.x, packet.y, packet.z, packet.entityID)
                            }
                            else -> {
                                return
                            }
                        }
                    }
                }
            }
        }
    }

    private /*inline*/ fun SafeClientEvent.checkBreakDamage(crystalX: Double, crystalY: Double, crystalZ: Double, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        val context = CombatManager.contextSelf ?: return false
        val selfDamage = max(context.calcDamage(crystalX, crystalY, crystalZ, false, mutableBlockPos), context.calcDamage(crystalX, crystalY, crystalZ, true, mutableBlockPos))
        if (player.scaledHealth - selfDamage <= noSuicide) return false

        val ticks = if (motionPredict) predictTicks else 0

        return when (breakCalculation) {
            BreakCalculation.Single -> {
                target?.let {
                    checkBreakDamage(crystalX, crystalY, crystalZ, selfDamage, getTargetInfo(it, ticks), mutableBlockPos)
                } ?: false
            }
            BreakCalculation.Multi -> {
                targets.get(100L).any {
                    checkBreakDamage(crystalX, crystalY, crystalZ, selfDamage, it, mutableBlockPos)
                }
            }
        }
    }

    private /*inline*/ fun SafeClientEvent.checkBreakDamage(crystalX: Double, crystalY: Double, crystalZ: Double, selfDamage: Float, targetInfo: TargetInfo, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        val targetDamage = calcDamage(targetInfo.entity, targetInfo.pos, targetInfo.box, crystalX, crystalY, crystalZ, mutableBlockPos)
        if (lethalOverride && targetDamage - targetInfo.entity.totalHealth > lethalBalance && targetDamage <= lethalMaxDamage) {
            return true
        }

        if (selfDamage > breakMaxSelfDamage) return false

        val minDamage: Float
        val balance: Float

        if (shouldForcePlace(targetInfo.entity)) {
            minDamage = forcePlaceMinDamage
            balance = forcePlaceBalance
        } else {
            minDamage = breakMinDamage
            balance = breakBalance
        }

        return targetDamage >= minDamage && targetDamage - selfDamage >= balance
    }

    private /*inline*/ fun SafeClientEvent.handlePlacedExplosion(posX: Double, posY: Double, posZ: Double, list: List<EntityEnderCrystal>) {
        placeInfo.getLazy()?.let {
            for (crystal in list) {
                if (CrystalUtils.placeBoxIntersectsCrystalBox(crystal.posX, crystal.posY, crystal.posZ, it.blockPos)) {
                    if (packetPlace.onRemove) placeDirect(it)
                    explosionCount++
                    return
                }
            }
        }

        if (placedPosMap.containsKey(toLong(posX, posY - 1.0, posZ))) {
            explosionCount++
        }
    }


    private /*inline*/ fun paused(): Boolean {
        return false // AnvilCity.isActive()
    }

    private /*inline*/ fun runLoop() {
        if (paused()) return

        val breakFlag = breakMode != BreakMode.Off && breakTimer.tick(breakDelay)
        val placeFlag = placeMode != PlaceMode.Off && placeTimer.tick(placeDelay)

        if (timeoutTimer.tickAndReset(5L)) {
            updateTimeouts()
        }

        if (breakFlag || placeFlag) {
            runSafe {
                playerController.syncCurrentPlayItems()
                val placeInfo = if (forceUpdate) placeInfo.get(5L) else placeInfo.get(updateDelay)
                placeInfo?.let {
                    if (checkPausing()) return@runSafe
                    if (breakFlag) doBreak(placeInfo)
                    if (placeFlag) doPlace(placeInfo)
                }
            }
        }
    }

    private /*inline*/ fun updateTimeouts() {
        val current = System.currentTimeMillis()

        synchronized(placedPosMap) {
            placedPosMap.values.removeIf {
                it < current
            }
        }

        synchronized(crystalSpawnMap) {
            crystalSpawnMap.values.removeIf {
                it + 5000L < current
            }
        }

        synchronized(attackedCrystalMap) {
            attackedCrystalMap.values.removeIf {
                it < current
            }
        }
    }

    private /*inline*/ fun SafeClientEvent.checkPausing(): Boolean {
        return eatingPause && player.isHandActive && player.activeItemStack.item is ItemFood || miningPause && playerController.isHittingBlock
    }

    private /*inline*/ fun SafeClientEvent.doBreak(placeInfo: PlaceInfo) {
        val crystalList = getCrystalList()

        val crystal = when (breakMode) {
            BreakMode.Own -> {
                getTargetCrystal(placeInfo, crystalList) ?: getCrystal(crystalList.filter {
                    placedPosMap.containsKey(toLong(it.posX, it.posY - 1.0, it.posZ))
                })
            }
            BreakMode.Target -> {
                getTargetCrystal(placeInfo, crystalList)
            }
            BreakMode.Smart -> {
                getTargetCrystal(placeInfo, crystalList) ?: getCrystal(crystalList)
            }
            BreakMode.All -> {
                val entity = target ?: player
                crystalList.minByOrNull { entity.getDistanceSq(it) }
            }
            else -> {
                return
            }
        }

        crystal?.let {
            breakDirect(it.posX, it.posY, it.posZ, it.entityId)
        }
    }

    private /*inline*/ fun SafeClientEvent.getCrystalList(): List<EntityEnderCrystal> {
        val eyePos = PlayerPacketManager.position.add(0.0, player.getEyeHeight().toDouble(), 0.0)
        val sight = eyePos.add(PlayerPacketManager.rotation.toViewVec().scale(8.0))
        val mutableBlockPos = BlockPos.MutableBlockPos()

        return EntityManager.entity.asSequence().filter { it.isEntityAlive }.filterIsInstance<EntityEnderCrystal>().runIf(
            bbtt
        ) {
                val current = System.currentTimeMillis()
                filter { current - getSpawnTime(it) >= bbttFactor }
            }.filter { checkBreakRange(it, mutableBlockPos) }.filter { checkCrystalRotation(it.entityBoundingBox, eyePos, sight) }.toList()
    }

    private /*inline*/ fun <T> T.runIf(boolean: Boolean, block: T.() -> T): T {
        return if (boolean) block.invoke(this)
        else this
    }

    private /*inline*/ fun getSpawnTime(crystal: EntityEnderCrystal): Long {
        return crystalSpawnMap.computeIfAbsent(crystal.entityId) {
            System.currentTimeMillis() - crystal.ticksExisted * 50
        }
    }

    private /*inline*/ fun getTargetCrystal(placeInfo: PlaceInfo, crystalList: List<EntityEnderCrystal>): EntityEnderCrystal? {
        return crystalList.firstOrNull {
            CrystalUtils.placeBoxIntersectsCrystalBox(it.posX, it.posY, it.posZ, placeInfo.blockPos)
        }
    }

    @Suppress("DuplicatedCode")
    private /*inline*/ fun SafeClientEvent.getCrystal(crystalList: List<EntityEnderCrystal>): EntityEnderCrystal? {
        val max = BreakInfo.Mutable()
        val safe = BreakInfo.Mutable()
        val lethal = BreakInfo.Mutable()

        val targets = targets.get().toList()
        val playerPos = PlayerPacketManager.position
        val playerBox = PlayerPacketManager.boundingBox
        val noSuicide = noSuicide
        val mutableBlockPos = BlockPos.MutableBlockPos()

        if (targets.isNotEmpty()) {
            for (crystal in crystalList) {
                val selfDamage = calcDamage(player, playerPos, playerBox, crystal.posX, crystal.posY, crystal.posZ, mutableBlockPos)
                if (player.scaledHealth - selfDamage <= noSuicide) continue
                if (!lethalOverride && selfDamage > breakMaxSelfDamage) continue

                for ((entity, entityPos, entityBox) in targets) {
                    val targetDamage = calcDamage(entity, entityPos, entityBox, crystal.posX, crystal.posY, crystal.posZ, mutableBlockPos)
                    if (lethalOverride && targetDamage - entity.totalHealth > lethalBalance && selfDamage < lethal.selfDamage && selfDamage <= lethalMaxDamage) {
                        lethal.update(crystal, selfDamage, targetDamage)
                    }

                    if (selfDamage > breakMaxSelfDamage) continue

                    val minDamage: Float
                    val balance: Float

                    if (shouldForcePlace(entity)) {
                        minDamage = forcePlaceMinDamage
                        balance = forcePlaceBalance
                    } else {
                        minDamage = breakMinDamage
                        balance = breakBalance
                    }

                    if (targetDamage >= minDamage && targetDamage - selfDamage >= balance) {
                        if (targetDamage > max.targetDamage) {
                            max.update(crystal, selfDamage, targetDamage)
                        } else if (max.targetDamage - targetDamage <= safeRange && max.selfDamage - selfDamage >= safeThreshold) {
                            safe.update(crystal, selfDamage, targetDamage)
                        }
                    }
                }
            }
        }

        if (max.targetDamage - safe.targetDamage > safeRange || max.selfDamage - safe.selfDamage <= safeThreshold) {
            safe.clear()
        }

        val valid = lethal.takeValid() ?: safe.takeValid() ?: max.takeValid()

        return valid?.crystal
    }

    @Suppress("UNUSED_PARAMETER")
    private /*inline*/ fun SafeClientEvent.checkCrystalRotation(x: Double, y: Double, z: Double): Boolean {
        if (!rotation) return true

        val eyePos = PlayerPacketManager.position.add(0.0, player.getEyeHeight().toDouble(), 0.0)
        val sight = eyePos.add(PlayerPacketManager.rotation.toViewVec().scale(8.0))

        return checkCrystalRotation(CrystalUtils.getCrystalBB(x, y, z), eyePos, sight)
    }

    private /*inline*/ fun checkCrystalRotation(box: AxisAlignedBB, eyePos: Vec3d, sight: Vec3d): Boolean {
        return !rotation || box.calculateIntercept(eyePos, sight) != null || breakRotationRange != 0.0f && checkRotationDiff(getRotationTo(eyePos, box.center), breakRotationRange)
    }

    private /*inline*/ fun SafeClientEvent.doPlace(placeInfo: PlaceInfo) {
        if (spamPlace || checkPlaceCollision(placeInfo)) {
            placeDirect(placeInfo)
        }
    }

    private /*inline*/ fun checkPlaceCollision(placeInfo: PlaceInfo): Boolean {
        return EntityManager.entity.asSequence().filter { it.isEntityAlive }.filterIsInstance<EntityEnderCrystal>().filter { CrystalUtils.placeBoxIntersectsCrystalBox(it.posX, it.posY, it.posZ, placeInfo.blockPos) }.filterNot { attackedCrystalMap.containsKey(it.entityId) }.none()
    }

    private /*inline*/ fun SafeClientEvent.placeDirect(placeInfo: PlaceInfo) {
        if (player.allSlots.countItem(Items.END_CRYSTAL) == 0) return

        val hand = getHandNullable()

        if (hand == null) {
            when (autoSwap) {
                SwapMode.Off -> {
                    return
                }
                SwapMode.Swap -> {
                    val slot = player.getCrystalSlot() ?: return
                    swapToSlot(slot)
                    connection.sendPacket(placePacket(placeInfo, EnumHand.MAIN_HAND))
                }
                SwapMode.Spoof -> {
                    val slot = player.getCrystalSlot() ?: return
                    val packet = placePacket(placeInfo, EnumHand.MAIN_HAND)
                    spoofHotbar(slot) {
                        connection.sendPacket(packet)
                    }
                }
            }
        } else {
            connection.sendPacket(placePacket(placeInfo, hand))
        }

        placedPosMap[placeInfo.blockPos.toLong()] = System.currentTimeMillis() + ownTimeout
        if (placeSwing) swingHand()
        placeTimer.reset()

        lastActiveTime = System.currentTimeMillis()
    }

    private /*inline*/ fun placePacket(placeInfo: PlaceInfo, hand: EnumHand): CPacketPlayerTryUseItemOnBlock {
        return CPacketPlayerTryUseItemOnBlock(placeInfo.blockPos, placeInfo.side, hand, placeInfo.hitVecOffset.x, placeInfo.hitVecOffset.y, placeInfo.hitVecOffset.z)
    }

    private /*inline*/ fun SafeClientEvent.breakDirect(x: Double, y: Double, z: Double, entityID: Int) {
        if (autoSwap != SwapMode.Spoof && antiWeakness != SwapMode.Spoof && System.currentTimeMillis() - club.eridani.epsilon.client.management.HotbarManager.swapTime < swapDelay * 50L) return

        if (player.isWeaknessActive() && !isHoldingTool()) {
            when (antiWeakness) {
                SwapMode.Off -> {
                    return
                }
                SwapMode.Swap -> {
                    val slot = getWeaponSlot() ?: return
                    swapToSlot(slot)
                    if (autoSwap != SwapMode.Spoof && swapDelay != 0) return
                    connection.sendPacket(attackPacket(entityID))
                    swingHand()
                }
                SwapMode.Spoof -> {
                    val slot = getWeaponSlot() ?: return
                    val packet = attackPacket(entityID)
                    spoofHotbar(slot) {
                        connection.sendPacket(packet)
                        swingHand()
                    }
                }
            }
        } else {
            connection.sendPacket(attackPacket(entityID))
            swingHand()
        }

        placeInfo.get(500L)?.let {
            if (packetPlace.onBreak && CrystalUtils.placeBoxIntersectsCrystalBox(x, y, z, it.blockPos)) {
                placeDirect(it)
            }
            player.setLastAttackedEntity(it.target)
        }
        attackedCrystalMap[entityID] = System.currentTimeMillis() + 1000L
        breakTimer.reset()

        lastActiveTime = System.currentTimeMillis()
    }

    private /*inline*/ fun attackPacket(entityID: Int): CPacketUseEntity {
        val packet = CPacketUseEntity()
        packet.packetAction = CPacketUseEntity.Action.ATTACK
        packet.id = entityID
        return packet
    }

    private /*inline*/ fun EntityPlayerSP.isWeaknessActive(): Boolean {
        return this.isPotionActive(MobEffects.WEAKNESS) && this.getActivePotionEffect(MobEffects.STRENGTH)?.let {
            it.amplifier <= 0
        } ?: true
    }

    private /*inline*/ fun SafeClientEvent.isHoldingTool(): Boolean {
        val item = player.serverSideItem.item
        return item is ItemTool || item is ItemSword
    }

    private /*inline*/ fun EntityPlayerSP.getCrystalSlot(): HotbarSlot? {
        return this.hotbarSlots.firstItem(Items.END_CRYSTAL)
    }

    private /*inline*/ fun SafeClientEvent.getWeaponSlot(): HotbarSlot? {
        return player.hotbarSlots.filterByStack {
            val item = it.item
            item is ItemSword || item is ItemTool
        }.maxByOrNull {
            val itemStack = it.stack
            itemStack.attackDamage
        }
    }

    @Suppress("DuplicatedCode")
    private /*inline*/ fun SafeClientEvent.calcPlaceInfo(checkRotation: Boolean): PlaceInfo? {
        val max = PlaceInfo.Mutable(player)
        val safe = PlaceInfo.Mutable(player)
        val lethal = PlaceInfo.Mutable(player)

        val targets = targets.get().toList()
        val playerPos = PlayerPacketManager.position
        val playerBox = PlayerPacketManager.boundingBox
        val noSuicide = noSuicide
        val mutableBlockPos = BlockPos.MutableBlockPos()
        val targetBlocks = getPlaceablePos(checkRotation, mutableBlockPos)

        if (targets.isNotEmpty()) {
            for (pos in targetBlocks) {
                val placeBox = CrystalUtils.getCrystalPlacingBB(pos)
                val crystalX = pos.x + 0.5
                val crystalY = pos.y + 1.0
                val crystalZ = pos.z + 0.5

                val selfDamage = calcDamage(player, playerPos, playerBox, crystalX, crystalY, crystalZ, mutableBlockPos)
                if (player.scaledHealth - selfDamage <= noSuicide) continue
                if (!lethalOverride && selfDamage > placeMaxSelfDamage) continue

                for ((entity, entityPos, entityBox, currentPos) in targets) {
                    if (entityBox.intersects(placeBox)) continue
                    if (placeBox.intersects(entityPos, currentPos)) continue

                    val targetDamage = calcDamage(entity, entityPos, entityBox, crystalX, crystalY, crystalZ, mutableBlockPos)
                    if (lethalOverride && targetDamage - entity.totalHealth > lethalBalance && selfDamage < lethal.selfDamage && selfDamage <= lethalMaxDamage) {
                        lethal.update(entity, pos, selfDamage, targetDamage)
                    }

                    if (selfDamage > placeMaxSelfDamage) continue

                    val minDamage: Float
                    val balance: Float

                    if (shouldForcePlace(entity)) {
                        minDamage = forcePlaceMinDamage
                        balance = forcePlaceBalance
                    } else {
                        minDamage = placeMinDamage
                        balance = placeBalance
                    }

                    if (targetDamage >= minDamage && targetDamage - selfDamage >= balance) {
                        if (targetDamage > max.targetDamage) {
                            max.update(entity, pos, selfDamage, targetDamage)
                        } else if (safeOverride && max.targetDamage - targetDamage <= safeRange && max.selfDamage - selfDamage >= safeThreshold) {
                            safe.update(entity, pos, selfDamage, targetDamage)
                        }
                    }
                }
            }
        }

        if (max.targetDamage - safe.targetDamage > safeRange || max.selfDamage - safe.selfDamage <= safeThreshold) {
            safe.clear(player)
        }

        val placeInfo = lethal.takeValid() ?: safe.takeValid() ?: max.takeValid()

        placeInfo?.calcPlacement(this)
        return placeInfo
    }

    private /*inline*/ fun SafeClientEvent.getTargets(): Sequence<TargetInfo> {
        val rangeSq = targetRange.sq
        val ticks = if (motionPredict) predictTicks else 0
        val list = ArrayList<TargetInfo>()
        val eyePos = PlayerPacketManager.eyePosition

        if (players) {
            for (target in EntityManager.players) {
                if (target === player) continue
                if (!target.isEntityAlive) continue
                if (FriendManager.isFriend(target)) continue
                if (target.distanceSqTo(eyePos) > rangeSq) continue

                list.add(getTargetInfo(target, ticks))
            }
        }

        if (mobs || animals) {
            for (target in EntityManager.entity) {
                if (target == player) continue
                if (!target.isEntityAlive) continue
                if (target !is EntityLivingBase) continue
                if (target is EntityPlayer) continue
                if (target.distanceSqTo(eyePos) > rangeSq) continue
                if (!animals && target.isPassive) continue

                val pos = target.positionVector
                list.add(TargetInfo(target, pos, target.entityBoundingBox, pos, Vec3d.ZERO))
            }
        }

        list.sortBy { player.getDistanceSq(it.entity) }

        return list.asSequence().filter { it.entity.isEntityAlive }.take(maxTargets)
    }

    private /*inline*/ fun SafeClientEvent.getTargetInfo(entity: EntityLivingBase, ticks: Int): TargetInfo {
        val motionX = (entity.posX - entity.lastTickPosX).coerceIn(-0.6, 0.6)
        val motionY = (entity.posY - entity.lastTickPosY).coerceIn(-0.5, 0.5)
        val motionZ = (entity.posZ - entity.lastTickPosZ).coerceIn(-0.6, 0.6)

        val entityBox = entity.entityBoundingBox
        var targetBox = entityBox

        for (tick in 0..ticks) {
            targetBox = canMove(targetBox, motionX, motionY, motionZ) ?: canMove(targetBox, motionX, 0.0, motionZ) ?: canMove(targetBox, 0.0, motionY, 0.0) ?: break
        }

        val offsetX = targetBox.minX - entityBox.minX
        val offsetY = targetBox.minY - entityBox.minY
        val offsetZ = targetBox.minZ - entityBox.minZ
        val motion = Vec3d(offsetX, offsetY, offsetZ)
        val pos = entity.positionVector

        return TargetInfo(entity, pos.add(motion), targetBox, pos, motion)
    }

    private /*inline*/ fun SafeClientEvent.canMove(box: AxisAlignedBB, x: Double, y: Double, z: Double): AxisAlignedBB? {
        return box.offset(x, y, z).takeIf { !world.collidesWithAnyBlock(it) }
    }

    private /*inline*/ fun SafeClientEvent.shouldForcePlace(entity: EntityLivingBase): Boolean {
        return (!forcePlaceSword || player.heldItemMainhand.item !is ItemSword) && (entity.totalHealth <= forcePlaceHealth || entity.realSpeed >= forcePlaceMotion || entity.getMinArmorRate() <= forcePlaceArmorRate)
    }

    private /*inline*/ fun EntityLivingBase.getMinArmorRate(): Int {
        return this.armorInventoryList.toList().asSequence().filter { it.isItemStackDamageable }.map { ((it.maxDamage - it.itemDamage) * 100.0f / it.maxDamage.toFloat()).toInt() }.maxOrNull() ?: 0
    }

    private /*inline*/ fun SafeClientEvent.getPlaceablePos(checkRotation: Boolean, mutableBlockPos: BlockPos.MutableBlockPos): List<BlockPos> {
        val range = placeRange

        val rangeSq = range.sq
        val wallRangeSq = wallRange.sq

        val single = placeMode == PlaceMode.Single
        val floor = range.fastFloor()
        val ceil = range.fastCeil()

        val list = ArrayList<BlockPos>()
        val pos = BlockPos.MutableBlockPos()

        val feetPos = PlayerPacketManager.position

        val feetXInt = feetPos.x.fastFloor()
        val feetYInt = feetPos.y.fastFloor()
        val feetZInt = feetPos.z.fastFloor()

        val eyePos = PlayerPacketManager.eyePosition
        val sight = eyePos.add(PlayerPacketManager.rotation.toViewVec().scale(8.0))

        for (x in feetXInt - floor..feetXInt + ceil) {
            for (z in feetZInt - floor..feetZInt + ceil) {
                for (y in feetYInt - floor..feetYInt + ceil) {
                    pos.setPos(x, y, z)
                    if (!world.worldBorder.contains(pos)) continue

                    val crystalX = pos.x + 0.5
                    val crystalY = pos.y + 1.0
                    val crystalZ = pos.z + 0.5

                    if (eyePos.squareDistanceTo(crystalX, crystalY, crystalZ) > rangeSq) continue
                    if (feetPos.squareDistanceTo(crystalX, crystalY, crystalZ) > wallRangeSq && !world.rayTraceVisible(eyePos, crystalX, crystalY + 1.7, crystalZ, 20, mutableBlockPos)) continue
                    if (!isValidPos(single, pos, mutableBlockPos)) continue
                    if (checkRotation && !checkPlaceRotation(pos, eyePos, sight)) continue

                    list.add(pos.toImmutable())
                }
            }
        }

        return list
    }

    private /*inline*/ fun SafeClientEvent.isValidPos(single: Boolean, pos: BlockPos, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        if (!isPlaceable(pos, newPlacement, mutableBlockPos)) {
            return false
        }

        val minX = pos.x + 0.001
        val minY = pos.y + 1.0
        val minZ = pos.z + 0.001
        val maxX = pos.x + 0.999
        val maxY = pos.y + 3.0
        val maxZ = pos.z + 0.999

        for (entity in EntityManager.entity) {
            if (!entity.isEntityAlive) continue
            if (!entity.entityBoundingBox.intersects(minX, minY, minZ, maxX, maxY, maxZ)) continue
            if (!single) return false
            if (entity !is EntityEnderCrystal) return false
            if (!checkBreakRange(entity, mutableBlockPos)) return false
        }

        return true
    }

    private /*inline*/ fun checkPlaceRotation(pos: BlockPos, eyePos: Vec3d, sight: Vec3d): Boolean {
        if (AxisAlignedBB(pos).calculateIntercept(eyePos, sight) != null) return true

        return placeRotationRange != 0.0f && checkRotationDiff(getRotationTo(eyePos, pos.toVec3dCenter()), placeRotationRange)
    }

    private /*inline*/ fun SafeClientEvent.getHandNullable(): EnumHand? {
        return when (Items.END_CRYSTAL) {
            player.heldItemOffhand.item -> EnumHand.OFF_HAND
            player.heldItemMainhand.item -> EnumHand.MAIN_HAND
            else -> null
        }
    }

    private /*inline*/ fun SafeClientEvent.swingHand() {
        val hand = when (swingHand) {
            SwingHand.Auto -> if (player.heldItemOffhand.item == Items.END_CRYSTAL) EnumHand.OFF_HAND else EnumHand.MAIN_HAND
            SwingHand.OffHand -> EnumHand.OFF_HAND
            SwingHand.MainHand -> EnumHand.MAIN_HAND
        }

        swingMode.swingHand(this, hand)
    }

    private /*inline*/ fun SafeClientEvent.checkBreakRange(entity: EntityEnderCrystal, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        return checkBreakRange(entity.posX, entity.posY, entity.posZ, mutableBlockPos)
    }

    private /*inline*/ fun SafeClientEvent.checkBreakRange(x: Double, y: Double, z: Double, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        return player.eyeDistanceSq(x, y, z) <= breakRange.sq && (player.getDistanceSq(x, y, z) <= wallRange.sq || world.rayTraceVisible(player.eyePosition, x, y + 1.7, z, 20, mutableBlockPos))
    }

    private /*inline*/ fun Entity.eyeDistanceSq(x: Double, y: Double, z: Double): Double {
        return distanceSq(this.posX, this.posY + this.eyeHeight, this.posZ, x, y, z)
    }

    private /*inline*/ fun toLong(x: Double, y: Double, z: Double): Long {
        return club.eridani.epsilon.client.util.math.vector.toLong(x.fastFloor(), y.fastFloor(), z.fastFloor())
    }

    private /*inline*/ fun calcDirection(eyePos: Vec3d, hitVec: Vec3d): EnumFacing {
        val x = hitVec.x - eyePos.x
        val y = hitVec.y - eyePos.y
        val z = hitVec.z - eyePos.z

        return EnumFacing.HORIZONTALS.maxByOrNull {
            x * it.directionVec.x + y * it.directionVec.y + z * it.directionVec.z
        } ?: EnumFacing.NORTH
    }

    private /*inline*/ fun checkRotationDiff(rotation: Vec2f, range: Float): Boolean {
        val serverSide = PlayerPacketManager.rotation
        return RotationUtils.calcAbsAngleDiff(rotation.x, serverSide.x) <= range && RotationUtils.calcAbsAngleDiff(rotation.y, serverSide.y) <= range
    }

    private /*inline*/ fun SafeClientEvent.isPlaceable(pos: BlockPos, newPlacement: Boolean, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        if (!canPlaceCrystalOn(pos)) {
            return false
        }
        val posUp = mutableBlockPos.setAndAdd(pos, 0, 1, 0)
        return if (newPlacement) {
            world.isAir(posUp)
        } else {
            CrystalUtils.isValidMaterial(world.getBlockState(posUp)) && CrystalUtils.isValidMaterial(world.getBlockState(posUp.add(0, 1, 0)))
        }
    }


    private val reductionMap = Collections.synchronizedMap(WeakHashMap<EntityLivingBase, DamageReduction>())

    private class DamageReduction(entity: EntityLivingBase) {
        private val armorValue: Float = entity.totalArmorValue.toFloat()
        private val toughness: Float
        private val resistance: Float
        private val blastReduction: Float

        init {
            toughness = entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
            val potionEffect = entity.getActivePotionEffect(MobEffects.RESISTANCE)
            resistance = if (potionEffect != null) max(1.0f - (potionEffect.amplifier + 1) * 0.2f, 0.0f) else 1.0f
            blastReduction = 1.0f - min(calcTotalEPF(entity), 20) / 25.0f
        }

        fun calcReductionDamage(damage: Float): Float {
            return CombatRules.getDamageAfterAbsorb(damage, armorValue, toughness) * resistance * blastReduction
        }

        companion object {
            private /*inline*/ fun calcTotalEPF(entity: EntityLivingBase): Int {
                var epf = 0
                for (itemStack in entity.armorInventoryList) {
                    val nbtTagList = itemStack.enchantmentTagList
                    for (i in 0 until nbtTagList.tagCount()) {
                        val nbtTagCompound = nbtTagList.getCompoundTagAt(i)
                        val id = nbtTagCompound.getInteger("id")
                        val level = nbtTagCompound.getShort("lvl").toInt()
                        if (id == 0) {
                            // Protection
                            epf += level
                        } else if (id == 3) {
                            // Blast protection
                            epf += level * 2
                        }
                    }
                }
                return epf
            }
        }
    }

    private const val DOUBLE_SIZE = 12.0f
    private const val DAMAGE_FACTOR = 42.0f

    private /*inline*/ fun SafeClientEvent.calcDamage(entity: EntityLivingBase, entityPos: Vec3d, entityBox: AxisAlignedBB, crystalX: Double, crystalY: Double, crystalZ: Double, mutableBlockPos: BlockPos.MutableBlockPos): Float {
        val isPlayer = entity is EntityPlayer
        if (isPlayer && world.difficulty == EnumDifficulty.PEACEFUL) return 0.0f
        var damage: Float

        damage = if (isPlayer && crystalY - entityPos.y > 1.5652173822904127 && isResistant(world.getBlockState(mutableBlockPos.setPos(crystalX.fastFloor(), crystalY.fastFloor() - 1, crystalZ.fastFloor())))) {
            1.0f
        } else {
            calcRawDamage(entityPos, entityBox, crystalX, crystalY, crystalZ, mutableBlockPos)
        }

        if (isPlayer) damage = calcDifficultyDamage(world, damage)
        return calcReductionDamage(entity, damage)
    }

    private /*inline*/ fun SafeClientEvent.calcRawDamage(entityPos: Vec3d, entityBox: AxisAlignedBB, posX: Double, posY: Double, posZ: Double, mutableBlockPos: BlockPos.MutableBlockPos): Float {
        val scaledDist = entityPos.distanceTo(posX, posY, posZ).toFloat() / DOUBLE_SIZE
        if (scaledDist > 1.0f) return 0.0f

        val factor = (1.0f - scaledDist) * getExposureAmount(entityBox, posX, posY, posZ, mutableBlockPos)
        return ((factor * factor + factor) * DAMAGE_FACTOR + 1.0f)
    }

    private val function: (BlockPos, IBlockState) -> FastRayTraceAction = { _, blockState ->
        if (blockState.block != Blocks.AIR && isResistant(blockState)) {
            FastRayTraceAction.CALC
        } else {
            FastRayTraceAction.SKIP
        }
    }

    private /*inline*/ fun SafeClientEvent.getExposureAmount(entityBox: AxisAlignedBB, posX: Double, posY: Double, posZ: Double, mutableBlockPos: BlockPos.MutableBlockPos): Float {
        val width = entityBox.maxX - entityBox.minX
        val height = entityBox.maxY - entityBox.minY

        val gridMultiplierXZ = 1.0 / (width * 2.0 + 1.0)
        val gridMultiplierY = 1.0 / (height * 2.0 + 1.0)

        val gridXZ = width * gridMultiplierXZ
        val gridY = height * gridMultiplierY

        val sizeXZ = (1.0 / gridMultiplierXZ).fastFloor()
        val sizeY = (1.0 / gridMultiplierY).fastFloor()
        val xzOffset = (1.0 - gridMultiplierXZ * (sizeXZ)) / 2.0

        var total = 0
        var count = 0

        for (yIndex in 0..sizeY) {
            for (xIndex in 0..sizeXZ) {
                for (zIndex in 0..sizeXZ) {
                    val x = gridXZ * xIndex + xzOffset + entityBox.minX
                    val y = gridY * yIndex + entityBox.minY
                    val z = gridXZ * zIndex + xzOffset + entityBox.minZ

                    total++
                    if (!world.fastRaytrace(x, y, z, posX, posY, posZ, 20, mutableBlockPos, function)) {
                        count++
                    }
                }
            }
        }

        return count.toFloat() / total.toFloat()
    }

    private /*inline*/ fun calcReductionDamage(entity: EntityLivingBase, damage: Float): Float {
        val reduction = reductionMap[entity]
        return reduction?.calcReductionDamage(damage) ?: damage
    }

    private /*inline*/ fun calcDifficultyDamage(world: WorldClient, damage: Float): Float {
        return when (world.difficulty) {
            EnumDifficulty.PEACEFUL -> 0.0f
            EnumDifficulty.EASY -> min(damage * 0.5f + 1.0f, damage)
            EnumDifficulty.HARD -> damage * 1.5f
            else -> damage
        }
    }


    private open class PlaceInfo(open val target: EntityLivingBase, open val blockPos: BlockPos, open val selfDamage: Float, open val targetDamage: Float, open val side: EnumFacing, open val hitVecOffset: Vec3f, open val hitVec: Vec3d) {
        class Mutable(target: EntityLivingBase) :
            PlaceInfo(target, BlockPos.ORIGIN, Float.MAX_VALUE, forcePlaceMinDamage, EnumFacing.UP, Vec3f.ZERO, Vec3d.ZERO) {
            override var target = target; private set
            override var blockPos = super.blockPos; private set
            override var selfDamage = super.selfDamage; private set
            override var targetDamage = super.targetDamage; private set
            override var side = super.side; private set
            override var hitVecOffset = super.hitVecOffset; private set
            override var hitVec = super.hitVec; private set

            fun update(target: EntityLivingBase, blockPos: BlockPos, selfDamage: Float, targetDamage: Float) {
                this.target = target
                this.blockPos = blockPos
                this.selfDamage = selfDamage
                this.targetDamage = targetDamage
            }

            fun clear(player: EntityPlayerSP) {
                update(player, BlockPos.ORIGIN, Float.MAX_VALUE, forcePlaceMinDamage)
            }

            fun calcPlacement(event: SafeClientEvent) {
                event {
                    when (placeBypass) {
                        PlaceBypass.Up -> {
                            side = EnumFacing.UP
                            hitVecOffset = Vec3f(0.5f, 1.0f, 0.5f)
                            hitVec = Vec3d(blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5)
                        }
                        PlaceBypass.Down -> {
                            side = EnumFacing.DOWN
                            hitVecOffset = Vec3f(0.5f, 0.0f, 0.5f)
                            hitVec = Vec3d(blockPos.x + 0.5, blockPos.y.toDouble(), blockPos.z + 0.5)
                        }
                        PlaceBypass.Closest -> {
                            side = calcDirection(player.eyePosition, blockPos.toVec3dCenter())
                            val directionVec = side.directionVec
                            val x = directionVec.x * 0.5f + 0.5f
                            val y = directionVec.y * 0.5f + 0.5f
                            val z = directionVec.z * 0.5f + 0.5f
                            hitVecOffset = Vec3f(x, y, z)
                            hitVec = blockPos.toVec3dCenter(x.toDouble(), y.toDouble(), z.toDouble())
                        }
                    }
                }
            }

            fun takeValid(): Mutable? {
                return this.takeIf {
                    target != mc.player && selfDamage != Float.MAX_VALUE && targetDamage != forcePlaceMinDamage
                }
            }
        }

        companion object {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            @JvmField
            val INVALID = PlaceInfo(object : EntityLivingBase(null) {
                override fun getArmorInventoryList(): MutableIterable<ItemStack> {
                    return ArrayList()
                }

                override fun setItemStackToSlot(slotIn: EntityEquipmentSlot, stack: ItemStack) {

                }

                override fun getItemStackFromSlot(slotIn: EntityEquipmentSlot): ItemStack {
                    return ItemStack.EMPTY
                }

                override fun getPrimaryHand(): EnumHandSide {
                    return EnumHandSide.RIGHT
                }
            }, BlockPos.ORIGIN, Float.NaN, Float.NaN, EnumFacing.UP, Vec3f.ZERO, Vec3d.ZERO)
        }
    }

    private open class BreakInfo(open val crystal: EntityEnderCrystal, open val selfDamage: Float, open val targetDamage: Float) {
        class Mutable : BreakInfo(DUMMY_CRYSTAL, Float.MAX_VALUE, forcePlaceMinDamage) {
            override var crystal = super.crystal; private set
            override var selfDamage = super.selfDamage; private set
            override var targetDamage = super.targetDamage; private set

            fun update(target: EntityEnderCrystal, selfDamage: Float, targetDamage: Float) {
                this.crystal = target
                this.selfDamage = selfDamage
                this.targetDamage = targetDamage
            }

            fun clear() {
                update(DUMMY_CRYSTAL, Float.MAX_VALUE, forcePlaceMinDamage)
            }
        }

        fun takeValid(): BreakInfo? {
            return this.takeIf {
                crystal !== DUMMY_CRYSTAL && selfDamage != Float.MAX_VALUE && targetDamage != forcePlaceMinDamage
            }
        }

        companion object {
            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            private val DUMMY_CRYSTAL = EntityEnderCrystal(null, 0.0, 0.0, 0.0)
        }
    }

    private data class TargetInfo(val entity: EntityLivingBase, val pos: Vec3d, val box: AxisAlignedBB, val currentPos: Vec3d, val predictMotion: Vec3d)

    private object Renderer {
        @JvmField
        var lastBlockPos: BlockPos? = null

        @JvmField
        var prevPos: Vec3d? = null

        @JvmField
        var currentPos: Vec3d? = null

        @JvmField
        var lastRenderPos: Vec3d? = null

        @JvmField
        var lastUpdateTime = 0L

        @JvmField
        var startTime = 0L

        @JvmField
        var scale = 0.0f

        @JvmField
        var lastSelfDamage = 0.0f

        @JvmField
        var lastTargetDamage = 0.0f

        fun reset() {
            lastBlockPos = null
            prevPos = null
            currentPos = null
            lastRenderPos = null
            lastUpdateTime = 0L
            startTime = 0L
            scale = 0.0f
            lastSelfDamage = 0.0f
            lastTargetDamage = 0.0f
        }

        fun onRender3D() {
            val filled = filledAlpha > 0
            val outline = outlineAlpha > 0
            val flag = filled || outline

            if (flag || targetDamage || selfDamage) {
                val placeInfo = renderPlaceInfo
                update(placeInfo)

                prevPos?.let { prevPos ->
                    currentPos?.let { currentPos ->
                        val multiplier = Easing.OUT_QUART.inc(Easing.toDelta(lastUpdateTime, movingLength))
                        val renderPos = prevPos.add(currentPos.subtract(prevPos).scale(multiplier.toDouble()))
                        scale = if (placeInfo != null) {
                            Easing.OUT_CUBIC.inc(Easing.toDelta(startTime, fadeLength))
                        } else {
                            Easing.IN_CUBIC.dec(Easing.toDelta(startTime, fadeLength))
                        }

                        val box = toRenderBox(renderPos, scale)
                        val color = if (rainbowColor) ColorUtils.rainbowHSB(100).toRGB() else GuiSetting.firstGuiColor.value
                        RenderUtils3D.drawFullBox(box.interp(), 1f, color.alpha(filledAlpha).toArgb(), color.alpha(
                            outlineAlpha
                        ).toArgb())

                        lastRenderPos = renderPos
                    }
                }
            }
            if (renderTarget) {
                val placeInfo = renderPlaceInfo
                if (placeInfo != null) {
                    RenderUtils3D.jelloRender(placeInfo.target)
                }
            }

        }

        fun onRender2D() {
            if (scale != 0.0f && (targetDamage || selfDamage)) {
                lastRenderPos?.let {
                    val text = buildString {
                        if (targetDamage) append("%.1f".format(lastTargetDamage))
                        if (selfDamage) {
                            if (this.isNotEmpty()) append('/')
                            append("%.1f".format(lastSelfDamage))
                        }
                    }

                    val screenPos = ProjectionUtils.toScreenPosScaled(it)

                    val alpha = (254.0f * scale).toInt() + 1

                    val color = if (scale == 1.0f)
                        ColorRGB(255, 255, 255)
                    else
                        ColorRGB(255, 255, 255, alpha)

                    MainFontRenderer.drawString(text, screenPos.x.toFloat() - MainFontRenderer.getWidth(text, 2.0f) * 0.5f, screenPos.y.toFloat() - MainFontRenderer.getHeight(2.0f) * 0.5f, color, 2.0f)
                }
            }
        }

        private /*inline*/ fun toRenderBox(vec3d: Vec3d, scale: Float): AxisAlignedBB {
            val halfSize = 0.5 * scale
            return AxisAlignedBB(vec3d.x - halfSize, vec3d.y - halfSize, vec3d.z - halfSize, vec3d.x + halfSize, vec3d.y + halfSize, vec3d.z + halfSize)
        }

        private /*inline*/ fun update(placeInfo: PlaceInfo?) {
            val newBlockPos = placeInfo?.blockPos
            if (newBlockPos != lastBlockPos) {
                if (placeInfo != null) {
                    currentPos = placeInfo.blockPos.toVec3dCenter()
                    prevPos = lastRenderPos ?: currentPos
                    lastUpdateTime = System.currentTimeMillis()
                    if (lastBlockPos == null) startTime = System.currentTimeMillis()
                } else {
                    lastUpdateTime = System.currentTimeMillis()
                    if (lastBlockPos != null) startTime = System.currentTimeMillis()
                }

                lastBlockPos = newBlockPos
            }

            if (placeInfo != null) {
                lastSelfDamage = placeInfo.selfDamage
                lastTargetDamage = placeInfo.targetDamage
            }
        }
    }
}