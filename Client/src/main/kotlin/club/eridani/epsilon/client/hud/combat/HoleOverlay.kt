package club.eridani.epsilon.client.hud.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.math.MathUtils
import net.minecraft.block.Block
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11.*


object HoleOverlay : HUDModule(
    name = "HoleOverlay",
    category = Category.CombatHUD,
    description = "Display hole"
) {

    init {
        resize {
            width = 48
            height = 48
        }
    }

    override fun onRender() {
        if (Utils.nullCheck()) return
        var yaw = 0f

        when ((mc.player.rotationYaw * 4.0 / 360.0 + 0.5).toInt() and 3) {
            1 -> yaw = 90f
            2 -> yaw = -180f
            3 -> yaw = -90f
        }

        val northPos = traceToBlock(mc.renderPartialTicks, yaw)
        val southPos = traceToBlock(mc.renderPartialTicks, yaw - 180.0f)
        val eastPos = traceToBlock(mc.renderPartialTicks, yaw + 90.0f)
        val westPos = traceToBlock(mc.renderPartialTicks, yaw - 90.0f)

        val north = getBlock(northPos)
        val south = getBlock(southPos)
        val east = getBlock(eastPos)
        val west = getBlock(westPos)


        if (north != Blocks.AIR) {
            drawBlock(north!!, x + 16f, y.toFloat())
        }

        if (south != Blocks.AIR) {
            drawBlock(south!!, x + 16f, y + 32f)
        }

        if (east != Blocks.AIR) {
            drawBlock(east!!, x + 32f, y + 16f)
        }

        if (west != Blocks.AIR) {
            drawBlock(west!!, x.toFloat(), y + 16f)
        }
    }


    private fun traceToBlock(partialTicks: Float, yaw: Float): BlockPos {
        val pos = MathUtils.getInterpolatedPos(mc.player, partialTicks)
        val dir = MathUtils.direction(yaw)
        return BlockPos(pos.x + dir.x, pos.y, pos.z + dir.y)
    }

    private fun getBlock(pos: BlockPos): Block? {
        val block: Block = mc.world.getBlockState(pos).block
        return if (block === Blocks.BEDROCK || block === Blocks.OBSIDIAN) {
            block
        } else {
            Blocks.AIR
        }
    }

    private fun drawBlock(block: Block, x: Float, y: Float) {
//        RenderUtil.drawRect(x, y, x + 16, y + 16, new Color(0, 0, 0, 255).getRGB());
        val stack = ItemStack(block)
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.translate(x, y, 0f)
        mc.renderItem.renderItemAndEffectIntoGUI(stack, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableBlend()
        GlStateManager.resetColor()
        GlStateManager.popMatrix()
    }

}
