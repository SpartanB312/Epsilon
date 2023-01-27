package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.threads.runSafe
import com.mojang.authlib.GameProfile
import net.minecraft.client.entity.EntityOtherPlayerMP
import java.util.*


object FakePlayer : Module(
    name = "FakePlayer",
    category = Category.Misc,
    description = "Test module"
) {

    val health = setting("Health", 10, 0..36, 1)

    override fun onEnable() {
        runSafe {
            val fakePlayer = EntityOtherPlayerMP(
                mc.world,
                GameProfile(UUID.fromString("60569353-f22b-42da-b84b-d706a65c5ddf"), "ICE_ASS")
            )
            fakePlayer.copyLocationAndAnglesFrom(mc.player)
            for (potionEffect in mc.player.activePotionEffects) {
                fakePlayer.addPotionEffect(potionEffect)
            }
            fakePlayer.health = health.value.toFloat()
            fakePlayer.inventory.copyInventory(mc.player.inventory)
            fakePlayer.rotationYawHead = mc.player.rotationYawHead
            mc.world.addEntityToWorld(-100, fakePlayer)
        }
    }

    override fun onDisable() {
        runSafe {
            mc.world.removeEntityFromWorld(-100)
        }
    }

}