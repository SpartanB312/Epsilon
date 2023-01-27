package club.eridani.epsilon.client.util.graphics.shaders

import club.eridani.epsilon.client.event.events.TickEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import org.lwjgl.input.Mouse

object ParticleShader : GLSLSandbox("/assets/minecraft/shaders/gui/Particle.fsh") {
    private val initTime = System.currentTimeMillis()
    private var prevMouseX = 0.0f
    private var prevMouseY = 0.0f
    private var mouseX = 0.0f
    private var mouseY = 0.0f

    init {
        listener<TickEvent>(true) {
            prevMouseX = mouseX
            prevMouseY = mouseY

            mouseX = Mouse.getX() - 1.0f
            mouseY = mc.displayHeight - Mouse.getY() - 1.0f
        }
    }

    fun render() {
        val deltaTicks = RenderUtils3D.partialTicks
        val width = mc.displayWidth.toFloat()
        val height = mc.displayHeight.toFloat()
        val mouseX = prevMouseX + (mouseX - prevMouseX) * deltaTicks
        val mouseY = prevMouseY + (mouseY - prevMouseY) * deltaTicks

        render(width, height, mouseX, mouseY, initTime)
    }
}