package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.InteractEvent
import club.eridani.epsilon.client.event.events.PlayerAttackEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.combat.CombatUtils
import club.eridani.epsilon.client.util.combat.CombatUtils.equipBestWeapon
import club.eridani.epsilon.client.util.inventory.equipBestTool
import net.minecraft.entity.EntityLivingBase

internal object AutoTool : Module(
    name = "AutoTool",
    description = "Automatically switch to the best tools when mining or attacking",
    category = Category.Misc
) {
    private val swapWeapon by setting("Switch Weapon", false)
    private val preferWeapon by setting("Prefer", CombatUtils.PreferWeapon.SWORD)

    init {
        safeListener<InteractEvent.Block.LeftClick> {
            if (!player.isCreative && world.getBlockState(it.pos).getBlockHardness(world, it.pos) != -1.0f) {
                equipBestTool(world.getBlockState(it.pos))
            }
        }

        safeListener<PlayerAttackEvent> {
            if (swapWeapon && it.entity is EntityLivingBase) {
                equipBestWeapon(preferWeapon)
            }
        }
    }
}