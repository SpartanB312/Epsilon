package club.eridani.epsilon.client.management

import net.minecraft.util.Util
import net.minecraft.util.Util.EnumOS
import org.lwjgl.opengl.Display
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object DisplayManager {

    fun setIcon(icon: Icon) {
        displayIcon(icon.iconName)
    }


    enum class Icon(val iconName: String) {
        IE("ie"),
        Epsilon("eps"),
        Old("old"),
    }


    private fun displayIcon(name: String) {
        if (Util.getOSType() != EnumOS.OSX) {
            try {
                Display.setIcon(
                    arrayOf(
                        readImageToBuffer(getResourceStream("/assets/epsilon/icon/${name}32.png")!!),
                        readImageToBuffer(getResourceStream("/assets/epsilon/icon/${name}16.png")!!)
                    )
                )
            } catch (ignore: IOException) {
            }
        }
    }

    private fun readImageToBuffer(imageStream: InputStream): ByteBuffer {
        val bufferImage = ImageIO.read(imageStream)
        val int = bufferImage.getRGB(
            0,
            0,
            bufferImage.width,
            bufferImage.height,
            null as IntArray?,
            0,
            bufferImage.width
        )
        val bytebuffer = ByteBuffer.allocate(4 * int.size)
        for (i in int) {
            bytebuffer.putInt(i shl 8 or i shr 24 and 255)
        }
        bytebuffer.flip()
        return bytebuffer
    }

    private fun getResourceStream(path: String): InputStream? {
        return DisplayManager::class.java.getResourceAsStream(path)
    }

}