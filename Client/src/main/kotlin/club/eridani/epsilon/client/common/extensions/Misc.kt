package club.eridani.epsilon.client.common.extensions

import club.eridani.epsilon.client.mixin.mixins.accessor.*
import club.eridani.epsilon.client.mixin.mixins.accessor.entity.AccessorEntity
import club.eridani.epsilon.client.mixin.mixins.accessor.entity.AccessorEntityTippedArrow
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityTippedArrow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.potion.PotionType
import net.minecraft.util.BitArray
import net.minecraft.util.Timer
import net.minecraft.world.chunk.BlockStateContainer
import net.minecraft.world.chunk.IBlockStatePalette

val Entity.isInWeb: Boolean get() = (this as AccessorEntity).epsilonIsInWeb()

fun Entity.getFlag(flag: Int) {
    (this as AccessorEntity).epsilonGetFlag(flag)
}

fun Entity.setFlag(flag: Int, value: Boolean) {
    (this as AccessorEntity).epsilonSetFlag(flag, value)
}

val EntityTippedArrow.potion: PotionType
    get() = (this as AccessorEntityTippedArrow).potion

val ItemTool.attackDamage get() = (this as AccessorItemTool).attackDamage

val Minecraft.timer: Timer get() = (this as AccessorMinecraft).epsilonGetTimer()
val Minecraft.renderPartialTicksPaused: Float get() = (this as AccessorMinecraft).renderPartialTicksPaused
var Minecraft.rightClickDelay: Int
    get() = (this as AccessorMinecraft).rightClickDelayTimer
    set(value) {
        (this as AccessorMinecraft).rightClickDelayTimer = value
    }

fun Minecraft.rightClickMouse() = (this as AccessorMinecraft).invokeRightClickMouse()

fun Minecraft.sendClickBlockToController(leftClick: Boolean) = (this as AccessorMinecraft).invokeSendClickBlockToController(leftClick)

var Timer.tickLength: Float
    get() = (this as AccessorTimer).epsilonGetTickLength()
    set(value) {
        (this as AccessorTimer).epsilonSetTickLength(value)
    }

fun KeyBinding.unpressKey() = (this as AccessorKeyBinding).`epsilon$invoke$unpressKey`()

val BlockStateContainer.storage: BitArray get() = (this as AccessorBlockStateContainer).epsilonGetStorage()

val BlockStateContainer.palette: IBlockStatePalette get() = (this as AccessorBlockStateContainer).epsilonGetPalette()

var ItemStack.stackSize
get() = (this as AccessorItemStack).stackSize
set(value) {
    (this as AccessorItemStack).stackSize = value
}