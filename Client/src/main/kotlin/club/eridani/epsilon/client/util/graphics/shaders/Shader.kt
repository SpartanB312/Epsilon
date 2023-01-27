@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package club.eridani.epsilon.client.util.graphics.shaders

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.util.Logger
import club.eridani.epsilon.client.util.graphics.GLObject
import club.eridani.epsilon.client.util.graphics.GlStateUtils
import org.lwjgl.opengl.GL20.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

open class Shader(vertShaderPath: String, fragShaderPath: String) : GLObject, Helper {
    final override val id: Int

    init {
        val vertexShaderID = createShader(vertShaderPath, GL_VERTEX_SHADER)
        val fragShaderID = createShader(fragShaderPath, GL_FRAGMENT_SHADER)
        val id = glCreateProgram()

        glAttachShader(id, vertexShaderID)
        glAttachShader(id, fragShaderID)

        glLinkProgram(id)
        val linked = glGetProgrami(id, GL_LINK_STATUS)
        if (linked == 0) {
            Logger.error(glGetProgramInfoLog(id, 1024))
            glDeleteProgram(id)
            throw IllegalStateException("Shader failed to link")
        }
        this.id = id

        glDetachShader(id, vertexShaderID)
        glDetachShader(id, fragShaderID)
        glDeleteShader(vertexShaderID)
        glDeleteShader(fragShaderID)
    }

    private fun createShader(path: String, shaderType: Int): Int {
        val srcString = javaClass.getResourceAsStream(path)!!.readBytes().decodeToString()
        val id = glCreateShader(shaderType)

        glShaderSource(id, srcString)
        glCompileShader(id)

        val compiled = glGetShaderi(id, GL_COMPILE_STATUS)
        if (compiled == 0) {
            Logger.error(glGetShaderInfoLog(id, 1024))
            glDeleteShader(id)
            throw IllegalStateException("Failed to compile shader: $path")
        }

        return id
    }

    override fun bind() {
        GlStateUtils.useProgram(id)
    }

    override fun unbind() {
        GlStateUtils.useProgram(0)
    }

    override fun destroy() {
        glDeleteProgram(id)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T : Shader> T.use(block: T.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    bind()
    block.invoke(this)
    GlStateUtils.useProgramForce(0)
}