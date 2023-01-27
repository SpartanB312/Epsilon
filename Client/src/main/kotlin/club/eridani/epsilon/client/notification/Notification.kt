package club.eridani.epsilon.client.notification

import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.client.NotificationRender
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.ResolutionHelper.width
import club.eridani.epsilon.client.util.graphics.font.renderer.IconRenderer
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getWidth
import club.eridani.epsilon.client.util.graphics.render.RenderTask
import club.eridani.epsilon.client.util.graphics.render.render.RectRenderTask
import club.eridani.epsilon.client.util.graphics.render.render.TextRenderTask
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import kotlin.math.sin

class Notification(var title: String = "", val module: AbstractModule? = null, private val message: String, val type: NotificationType, private val duration: Int = NotificationRender.duration) {
    private var currentTime: Long = 0
    private var startTime = System.currentTimeMillis()
    private var currentLocation: Float = width.toFloat()
    private var currentLastLocation: Int = width

    private val notificationHeight = 44
    private val spacing = 8
    private var lastYLocation = (indexOf(this) * notificationHeight).toFloat()
    private val fontColor = ColorRGB(255, 255, 255, 255)
    private val firstColor = ColorRGB(47, 49, 54, 255)
    private val secondColor = ColorRGB(32, 34, 37, 255)

    init {
        if (title.isEmpty()) title = when (type) {
            NotificationType.INFO -> "Notification"
            NotificationType.DEBUG -> "Debug"
            NotificationType.WARNING -> "Warning"
            NotificationType.MODULE -> "Module"
        }
    }

    private var renderTasks = mutableListOf<RenderTask>()

    fun update() {

        val tempRenderTasks = mutableListOf<RenderTask>()

        val index = indexOf(this)
        val sr = ScaledResolution(Minecraft.getMinecraft())
        currentTime++
        val notificationWidth = getWidth(message) + 58
        currentLocation = if (currentTime > duration * 50L - 50) (sr.scaledWidth + spacing).toFloat()
        else sr.scaledWidth - notificationWidth

        if (currentLastLocation.toFloat() != currentLocation) {
            val diff = currentLocation - currentLastLocation
            currentLocation = currentLastLocation.toFloat()
            currentLastLocation += (diff / 8).toInt()
        }

        if (currentTime > duration * 50L) {
            NotificationManager.notifications.remove(this)
        }

        val yLocation = (notificationHeight * index).toFloat() + NotificationRender.yDisplacement
        lastYLocation += NotificationRender.yDisplacement
        if (lastYLocation != yLocation) {
            val diff = yLocation - lastYLocation
            lastYLocation += diff / 3
        }

        val progress = currentTime.toFloat() / (duration * 50).toFloat()
        var barColor = GUIManager.firstColor.alpha(255)

        if (type == NotificationType.WARNING) {
            val i = 0.coerceAtLeast(255.coerceAtMost((sin(startTime - System.currentTimeMillis() / 100.0) * 255.0 / 2 + 127.5).toInt()))
            barColor = ColorRGB(i, 0, 0, 220)
        } else if (type == NotificationType.DEBUG) {
            barColor = ColorRGB(204, 193, 0)
        }

        tempRenderTasks.add(RectRenderTask(currentLocation, sr.scaledHeight - notificationHeight - lastYLocation, sr.scaledWidth.toFloat(), sr.scaledHeight - spacing - lastYLocation, secondColor))
        tempRenderTasks.add(RectRenderTask(currentLocation + 36, sr.scaledHeight - notificationHeight - lastYLocation, sr.scaledWidth.toFloat(), sr.scaledHeight - spacing - lastYLocation, firstColor))
        tempRenderTasks.add(object : RenderTask {
            override fun onRender() {
                RenderUtils2D.drawRectFilled(currentLocation + 36 + (notificationWidth * progress), sr.scaledHeight - spacing - lastYLocation, sr.scaledWidth.toFloat(), sr.scaledHeight - spacing - lastYLocation - 2, barColor)
            }
        })

        //Title
        tempRenderTasks.add(TextRenderTask(title, currentLocation + 30 + 16, sr.scaledHeight - 33 - lastYLocation - 4, ColorRGB(255, 255, 255, 255), 1.1f, isShadow = false))
        //Message
        tempRenderTasks.add(TextRenderTask(message, currentLocation + 30 + 16, sr.scaledHeight - 33 - lastYLocation + 8, ColorRGB(255, 255, 255, 255), isShadow = false))
        when (type) {
            NotificationType.MODULE -> {
                if (module != null && module.category.iconCode.isNotEmpty()) {
                    val iconCode = module.category.iconCode
                    val xAdd = when (iconCode) {
                        "9", "}" -> -1
                        "[" -> 3
                        "@" -> 1
                        else -> 0
                    }
                    val yAdd = when (iconCode) {
                        "@" -> 2
                        "a", "}" -> 1
                        else -> 0
                    }
                    tempRenderTasks.add(TextRenderTask(iconCode, currentLocation + IconRenderer.getWidth(iconCode) / 2f + 4 + xAdd, sr.scaledHeight - notificationHeight - lastYLocation + IconRenderer.getHeight(2.3f) / 2f - 6 + yAdd, fontColor, 2.3f, false, IconRenderer))
                }
            }
            NotificationType.INFO -> {
                tempRenderTasks.add(TextRenderTask("I", currentLocation + IconRenderer.getWidth("I") / 2f + 1.5f, sr.scaledHeight - notificationHeight - lastYLocation + IconRenderer.getHeight(2.3f) / 2f - 4, fontColor, 2.3f, false, IconRenderer))
            }
            NotificationType.WARNING -> {
                tempRenderTasks.add(TextRenderTask("!", currentLocation + IconRenderer.getWidth("!") / 2f + 2, sr.scaledHeight - notificationHeight - lastYLocation + IconRenderer.getHeight(2.3f) / 2f - 4, fontColor, 2.3f, false, IconRenderer))
            }
            NotificationType.DEBUG -> {
                tempRenderTasks.add(TextRenderTask("6", currentLocation + IconRenderer.getWidth("6") / 2f + 3, sr.scaledHeight - notificationHeight - lastYLocation + IconRenderer.getHeight(2.3f) / 2f - 4, fontColor, 2.3f, false, IconRenderer))
            }
        }

        renderTasks = tempRenderTasks

    }

    fun draw() {
        ArrayList(renderTasks).forEach {
            it.onRender()
        }
    }

    private fun indexOf(o: Any?): Int {
        if (o == null) {
            for (i in NotificationManager.notifications.indices) if (NotificationManager.notifications.toTypedArray()[i] == null) return i
        } else {
            for (i in NotificationManager.notifications.indices) if (o == NotificationManager.notifications.toTypedArray()[i]) return i
        }
        return -1
    }
}