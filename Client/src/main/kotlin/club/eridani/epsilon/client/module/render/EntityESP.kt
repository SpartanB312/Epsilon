package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.RenderEntityModelEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.entity.item.*
import net.minecraft.entity.monster.EntityGhast
import net.minecraft.entity.monster.EntityGolem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntityBat
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glLineWidth

object EntityESP :
    Module(name = "EntityESP", alias = arrayOf("ESP"), category = Category.Render, description = "Draw player render") {
    private val players = setting("Players", true)
    private val mobs = setting("Mobs", false)
    private val animals = setting("Animals", false)
    private val crystal = setting("EndCrystal", false)
    private val item = setting("Item", false)
    private val experience = setting("ExpBottle", false)
    private val pearl = setting("Pearl", false)
    private val invisible by setting("Invisible", true)
    var alpha by setting("Alpha", 255, 0..255, 1)
    private val width by setting("Width", 0.5f, 0.1f..5.0f, 0.1f)

    init {
        onRender3D { event ->
            runSafe {
                val fancyGraphics = mc.gameSettings.fancyGraphics
                mc.gameSettings.fancyGraphics = false
                val gamma = mc.gameSettings.gammaSetting
                mc.gameSettings.gammaSetting = 10000.0f

                mc.world.loadedEntityList.filter { isItemValid(it) }.forEach { entity ->
                    val entityShadow = mc.gameSettings.entityShadows
                    mc.gameSettings.entityShadows = false

                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.renderOne(width)
                    mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
                    glLineWidth(width)
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.renderTwo()
                    mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
                    glLineWidth(width)
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.renderThree()
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.renderFour(GUIManager.firstColor.alpha(alpha))
                    mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
                    glLineWidth(width)
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.renderFive()
                    GL11.glColor4f(1f, 1f, 1f, 1f)

                    mc.gameSettings.entityShadows = entityShadow
                }

                runCatching {
                    mc.gameSettings.fancyGraphics = fancyGraphics
                    mc.gameSettings.gammaSetting = gamma
                }
            }
        }

        safeListener<RenderEntityModelEvent> { event ->
            if (isEntityValid(event.entity)) {
                val entityShadow = mc.gameSettings.entityShadows
                mc.gameSettings.entityShadows = false

                event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale)
                RenderUtils3D.renderOne(width)
                event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale)
                glLineWidth(width)
                RenderUtils3D.renderTwo()
                event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale)
                glLineWidth(width)
                RenderUtils3D.renderThree()
                RenderUtils3D.renderFour(GUIManager.firstColor.alpha(alpha))
                event.modelBase.render(event.entity, event.limbSwing, event.limbSwingAmount, event.age, event.headYaw, event.headPitch, event.scale)
                glLineWidth(width)
                RenderUtils3D.renderFive()
                GL11.glColor4f(1f, 1f, 1f, 1f)

                mc.gameSettings.entityShadows = entityShadow
                event.cancel()
            }

        }
    }

    private fun isEntityValid(entity: Entity?): Boolean {
        return (entity != null && entity != mc.player && mc.renderViewEntity != entity && !entity.isDead &&
                (players.value && entity is EntityPlayer || mobs.value && (entity is EntityMob || entity is EntityVillager || entity is EntitySlime || entity is EntityGhast || entity is EntityDragon) || animals.value && (entity is EntityAnimal || entity is EntitySquid || entity is EntityGolem || entity is EntityBat))
                && (!entity.isInvisible || invisible))
    }

    private fun isItemValid(entity: Entity?): Boolean {
        return (entity != null && entity != mc.player && mc.renderViewEntity != entity && !entity.isDead &&
                (item.value && entity is EntityItem || experience.value && (entity is EntityXPOrb || entity is EntityExpBottle || pearl.value && entity is EntityEnderPearl || crystal.value && entity is EntityEnderCrystal))
                && (!entity.isInvisible || invisible))
    }
}