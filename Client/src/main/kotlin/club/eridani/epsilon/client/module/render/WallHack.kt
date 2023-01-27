package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraftforge.common.ForgeModContainer


object WallHack :
    Module(name = "WallHack", category = Category.Render, description = "Set walls transparent") {

    val opacity = setting("Opacity", 120, 0..255, 1)
    val light = setting("Light", 0, 0..100, 1)
    private val reloadMode = setting("Reload", Reload.All)

    val blocks = mutableListOf<Block>(Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE, Blocks.DIAMOND_ORE,

        Blocks.COAL_BLOCK, Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.LAPIS_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.DIAMOND_BLOCK,

        Blocks.IRON_BARS, Blocks.REDSTONE_LAMP, Blocks.LIT_REDSTONE_LAMP, Blocks.FURNACE, Blocks.LIT_FURNACE, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST)


    init {
        opacity.valueListen { _, _ ->
            reload()
        }
        light.valueListen { _, _ ->
            reload()
        }
    }

    override fun onEnable() {
        ForgeModContainer.forgeLightPipelineEnabled = false
        reload()
    }

    override fun onDisable() {
        reload()
        ForgeModContainer.forgeLightPipelineEnabled = true
    }


    private fun reload() {
        mc.renderChunksMany = true
        if (reloadMode.value == Reload.All) {
            mc.renderGlobal.loadRenderers()
        } else {
            val pos = mc.player.positionVector
            val dist = mc.gameSettings.renderDistanceChunks * 16
            mc.renderGlobal.markBlockRangeForRenderUpdate(pos.x.toInt() - dist,
                pos.y.toInt() - dist, pos.z.toInt() - dist,
                pos.x.toInt() + dist,
                pos.y.toInt() + dist, pos.z.toInt() + dist)
        }
    }

    enum class Reload {
        Soft, All
    }
}