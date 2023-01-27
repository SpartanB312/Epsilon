package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.OnUpdateWalkingPlayerEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.FriendManager
import club.eridani.epsilon.client.management.FriendManager.isFriend
import club.eridani.epsilon.client.mixin.mixins.accessor.AccessorMinecraft
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ItemUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.math.RayTraceResult
import org.lwjgl.input.Mouse


object MiddleClick : Module(
    name = "MiddleClick",
    category = Category.Misc,
    description = "Bind you mouse middle button with some feature",
    visibleOnArray = false
) {
    private var friend by setting("Friend", true)
    private var pearl by setting("Pearl", false)
    private var lastSlot = 0
    private var clicked = false

    init {
        listener<OnUpdateWalkingPlayerEvent.Pre> {
            if (!Mouse.isButtonDown(2)) {
                clicked = false
                return@listener
            }

            val result = mc.objectMouseOver ?: return@listener

            if (!clicked) {
                clicked = true
                if (friend && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit is EntityPlayer) {
                    if (isFriend(result.entityHit)) {
                        FriendManager.removeFriend(result.entityHit)
                    } else {
                        FriendManager.addFriend(result.entityHit)
                    }
                } else if (pearl && ItemUtil.findItemInHotBar(Items.ENDER_PEARL) != -1 && result.typeOfHit == RayTraceResult.Type.MISS) {
                    lastSlot = mc.player.inventory.currentItem
                    mc.player.inventory.currentItem = ItemUtil.findItemInHotBar(Items.ENDER_PEARL)
                    (mc as AccessorMinecraft).invokeRightClickMouse()
                    mc.player.inventory.currentItem = lastSlot
                }
            }
        }
    }

    override fun onDisable() {
        lastSlot = -1
    }
}