package club.eridani.epsilon.client.util.extension

import club.eridani.epsilon.client.util.math.fastFloor
import club.eridani.epsilon.client.util.math.vector.distance
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

inline val Entity.flooredPosition get() = BlockPos(posX.fastFloor(), posY.fastFloor(), posZ.fastFloor())

inline val Entity.betterPosition get() = BlockPos(posX.fastFloor(), (posY + 0.5).fastFloor(), posZ.fastFloor())

inline val Entity?.eyesPosition: Vec3d
    get() =
        Vec3d(
            this?.posX ?: 0.0,
            (this?.entityBoundingBox?.minY ?: 0.0) + (this?.eyeHeight?.toDouble() ?: 0.0),
            this?.posZ ?: 0.0
        )

inline val Entity.eyePosition get() = Vec3d(this.posX, this.posY + this.eyeHeight, this.posZ)

inline val Entity.prevPosVector get() = Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ)

inline val EntityLivingBase.scaledHealth: Float
    get() = this.health + this.absorptionAmount * (this.health / this.maxHealth)

inline val EntityLivingBase.totalHealth: Float
    get() = this.health + this.absorptionAmount

inline val Entity.realSpeed: Double
    get() = distance(this.posX, this.posZ, this.lastTickPosX, this.lastTickPosZ)