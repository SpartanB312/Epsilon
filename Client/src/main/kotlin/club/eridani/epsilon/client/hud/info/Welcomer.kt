package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.TextureManager
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import java.util.*

object Welcomer : HUDModule(name = "Welcomer", category = Category.InfoHUD, description = "Welcome to Epsilon") {

    val mode by setting("Mode", Mode.NiceDay)
    private var lastMessage = ""

    private inline val message
        get() = when (mode) {
            Mode.NiceDay -> {
                ("Welcome ${Wrapper.player?.name}! Have a nice day :)").also {
                    if (lastMessage != it) {
                        lastMessage = it
                        resize {
                            width = MainFontRenderer.getWidth(it).toInt()
                            height = MainFontRenderer.getHeight().toInt()
                        }
                    }
                }
            }
            Mode.Time -> (getTimeOfDay() + Wrapper.player?.name).also {
                if (lastMessage != it) {
                    lastMessage = it
                    resize {
                        width = MainFontRenderer.getWidth(it).toInt()
                        height = MainFontRenderer.getHeight().toInt()
                    }
                }
            }
            Mode.UwU -> ("Hello " + Wrapper.player?.name + " uwu").also {
                if (lastMessage != it) {
                    lastMessage = it
                    resize {
                        width = MainFontRenderer.getWidth(it).toInt()
                        height = MainFontRenderer.getHeight().toInt()
                    }
                }
            }
            Mode.DotGod -> ("Hello " + Wrapper.player?.name + " :^)").also {
                if (lastMessage != it) {
                    lastMessage = it
                    resize {
                        width = MainFontRenderer.getWidth(it).toInt()
                        height = MainFontRenderer.getHeight().toInt()
                    }
                }
            }
            Mode.Handsome -> ("Hi handsome boy " + Wrapper.player?.name).also {
                if (lastMessage != it) {
                    lastMessage = it
                    resize {
                        width = MainFontRenderer.getWidth(it).toInt()
                        height = MainFontRenderer.getHeight().toInt()
                    }
                }
            }
        }

    private fun getTimeOfDay(): String {
        val c = Calendar.getInstance()
        val timeOfDay: Int = c.get(Calendar.HOUR_OF_DAY)
        when {
            timeOfDay == 0 -> {
                return "Is Midnight "
            }
            timeOfDay < 6 -> {
                return "Good Early Morning "
            }
            timeOfDay < 12 -> {
                return "Good Morning "
            }
            timeOfDay < 16 -> {
                return "Good Afternoon "
            }
            timeOfDay < 19 -> {
                return "Good Evening "
            }
            timeOfDay < 24 -> {
                return "Good Night "
            }
            else -> return "Is Midnight "
        }
    }

    override fun onRender() {
        TextureManager.renderTextShadow(x - 10, y - 10 + (MainFontRenderer.getHeight() / 2).toInt(), width + 10, height + 10)
        MainFontRenderer.drawStringWithShadow(message, x.toFloat(), y.toFloat(), GUIManager.firstColor)
    }

    enum class Mode {
        Time, UwU, DotGod, Handsome, NiceDay
    }
}