package club.eridani.epsilon.client.util.graphics.particles

import org.lwjgl.opengl.Display
import org.lwjgl.util.vector.Vector2f
import java.util.*

class Particle(private var velocity: Vector2f, x: Float, y: Float, var size: Float) {
    private val pos: Vector2f = Vector2f(x, y)
    var alpha = 0f
        private set

    fun getDistanceSqTo(particle1: Particle): Float {
        return getDistanceSqTo(particle1.x, particle1.y)
    }

    private fun getDistanceSqTo(f: Float, f2: Float): Float {
        return ParticleSystem.distanceSq(x, y, f, f2)
    }

    var x: Float
        get() = pos.getX()
        set(f) {
            pos.setX(f)
        }
    var y: Float
        get() = pos.getY()
        set(f) {
            pos.setY(f)
        }

    fun tick(delta: Int, speed: Float) {
        pos.x += velocity.getX() * delta * speed
        pos.y += velocity.getY() * delta * speed
        if (alpha < 255.0f) alpha += 0.05f * delta
        if (pos.getX() > Display.getWidth()) pos.setX(0f)
        if (pos.getX() < 0) pos.setX(Display.getWidth().toFloat())
        if (pos.getY() > Display.getHeight()) pos.setY(0f)
        if (pos.getY() < 0) pos.setY(Display.getHeight().toFloat())
    }

    companion object {
        @JvmStatic
        fun generateParticle(): Particle {
            val velocity = Vector2f((Math.random() * 2.0f - 1.0f).toFloat(), (Math.random() * 2.0f - 1.0f).toFloat())
            val x = Random().nextInt(Display.getWidth()).toFloat()
            val y = Random().nextInt(Display.getHeight()).toFloat()
            val size = (Math.random() * 5.0f).toFloat() + 2.0f
            return Particle(velocity, x, y, size)
        }
    }

}