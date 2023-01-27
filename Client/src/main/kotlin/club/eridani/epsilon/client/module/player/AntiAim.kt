package club.eridani.epsilon.client.module.player

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.OnUpdateWalkingPlayerEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.PlayerPacketManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.math.Vec2f
import org.apache.commons.lang3.RandomUtils
import kotlin.random.Random

object AntiAim : Module(name = "AntiAim", category = Category.Player, description = "Anti aim with csgo hack!") {

    private val mode by setting("Mode", Mode.Spin)
    private val spinSpeed by setting("SpinSpeed", 10f, 1f..50f, 1f) { mode == Mode.Spin }
    private val down by setting("LockPitch", false)
    val serverSide by setting("ServerSide", false)
    var fakeYaw = 0f
    var fakePitch = 0f

    init {
        listener<OnUpdateWalkingPlayerEvent.Pre> {
            fakePitch = if (down) {
                90f
            } else {
                mc.player.rotationPitch
            }

            when (mode) {
                Mode.Spin -> {
                    fakeYaw += spinSpeed
                }
                Mode.Reverse -> {
                    fakeYaw = mc.player.rotationYaw + 180.0f
                    fakePitch = mc.player.rotationPitch + 180.0f
                }
                Mode.ReverseJitter -> {
                    fakeYaw = mc.player.rotationYaw + if (Random.nextBoolean()) -RandomUtils.nextFloat(0f, 45f) else RandomUtils.nextFloat(0f, 45f)
                    fakeYaw += 180.0f
                    if (!down) fakePitch = mc.player.rotationPitch + 180.0f
                }
                Mode.FakeJitter -> {
                    fakeYaw = mc.player.rotationYaw + if (Random.nextBoolean()) -RandomUtils.nextFloat(0f, 45f) else RandomUtils.nextFloat(0f, 45f)
                }
                Mode.Clown -> {
                    fakeYaw += if (Random.nextBoolean()) -RandomUtils.nextFloat(0f, 45f) else RandomUtils.nextFloat(0f, 45f)
                    fakePitch = -179.0f
                }
                Mode.Zero -> {
                    fakeYaw = mc.player.rotationYaw
                    fakePitch = -179.0f
                }
            }

            if (fakePitch != mc.player.rotationPitch || fakeYaw != mc.player.rotationYaw) {
                if (serverSide)
                PlayerPacketManager.sendPacket(-1) {
                    rotate(Vec2f(fakeYaw, fakePitch))
                } else {
                    mc.player.rotationYawHead = fakeYaw
                    mc.player.renderYawOffset = fakeYaw
                }
            }
        }
    }


    override fun onEnable() {
        if (Utils.nullCheck()) return
        fakeYaw = mc.player.rotationYaw
        fakePitch = mc.player.rotationPitch
    }

    enum class Mode {
        Spin, Reverse, ReverseJitter, FakeJitter, Zero, Clown
    }
}