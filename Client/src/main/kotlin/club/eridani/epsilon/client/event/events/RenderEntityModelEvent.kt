package club.eridani.epsilon.client.event.events

import club.eridani.epsilon.client.event.Cancellable
import net.minecraft.client.model.ModelBase
import net.minecraft.entity.Entity

class RenderEntityModelEvent(
    var modelBase: ModelBase,
    var entity: Entity,
    var limbSwing: Float,
    var limbSwingAmount: Float,
    var age: Float,
    var headYaw: Float,
    var headPitch: Float,
    var scale: Float
) : Cancellable()

