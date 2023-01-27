package club.eridani.epsilon.client.util.graphics

import club.eridani.epsilon.client.util.Wrapper
import net.minecraft.client.gui.ScaledResolution

object ResolutionHelper {
    val height get() = ScaledResolution(Wrapper.mc).scaledHeight
    val width get() = ScaledResolution(Wrapper.mc).scaledWidth
}