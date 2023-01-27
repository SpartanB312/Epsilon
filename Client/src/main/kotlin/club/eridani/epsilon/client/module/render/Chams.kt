package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.RenderEntityEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.EntityUtil
import club.eridani.epsilon.client.util.onRender3D
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11

object Chams : Module(
    name = "Chams",
    category = Category.Render,
    description = "See entities through walls"
) {

    private val players by setting("Player", true)
    private val animals by setting("Animal", false)
    private val mobs by setting("Mob", false)


    private fun isValidEntity(entity: Entity): Boolean {
        return entity is EntityLivingBase && players && entity is EntityPlayer || if (EntityUtil.isPassive(
                entity
            )
        ) animals else mobs
    }

    init {
        listener<RenderEntityEvent.All.Pre> {
            if (isValidEntity(it.entity)) {
                GL11.glDepthRange(0.0, 0.01)
            }
        }
        listener<RenderEntityEvent.All.Post> {
            if (isValidEntity(it.entity)) {
                GL11.glDepthRange(0.0, 1.0)
            }
        }

        onRender3D {

        }
    }

}