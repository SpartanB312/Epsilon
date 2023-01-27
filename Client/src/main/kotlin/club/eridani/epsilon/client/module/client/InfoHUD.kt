package club.eridani.epsilon.client.module.client

import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.tickLength
import club.eridani.epsilon.client.common.extensions.timer
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.ModuleManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.setting.GuiSetting
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.graphics.render.AsyncRenderer
import club.eridani.epsilon.client.util.graphics.render.asyncRender
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.threads.runSafe
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.pow

object InfoHUD :
    Module(name = "HUD", category = Category.Client, description = "Displays the HUD", visibleOnArray = false) {

    private val bindOnlyArray by setting("BindOnlyArray", false)
    private val arrayList by setting("Arraylist", true)
    private val sort by setting("SortList", true) { arrayList }
    private val rect by setting("Rect", RectMode.None) { arrayList }
    private val alphaRect by setting("Rect Opacity", 127, 0..255, 1) { arrayList && rect != RectMode.None }
    private val listHeight by setting("ListHeight", 11.0, 10.0..15.0, .1) { arrayList }
    private val listPos by setting("ListPos", ListPos.Top) { arrayList }
    private val potionLogo by setting("PotionHUD", PotionHUD.Down)
    private val coordinates by setting("Coordinates", true)
    private val netherCoords by setting("NetherCoords", true) { coordinates }
    private val direction by setting("Direction", true) { coordinates }
    private val potionEffect by setting("PotionEffect", true)
    private val potionPos by setting("PotionPos", Position.BotRight) { potionEffect }
    private val info by setting("Info", true)
    private val infoPos by setting("InfoPos", Position.BotRight) { info }
    private val displayIp by setting("Ip", true) { info }
    private val displayPing by setting("Ping", true) { info }
    private val displayFps by setting("Fps", true) { info }
    private val displayTps by setting("Tps", true) { info }
    private val countPlayer by setting("Player", true) { info }
    private val displaySpeed by setting("Speed", true) { info }
    private val speedUnit by setting("SpeedUtil", SpeedUtil.Km) { displaySpeed && info }
    private val displayTime by setting("Time", false) { info }
    private val displayDurability by setting("Durability", true) { info }

    private val formatter = DecimalFormat("#.#")
    private var animation = 0.0
    private val animationTimer = Timer()
    private val displayModules: Sequence<ModuleRenderInfo>
        get() = ModuleManager.modules.asSequence().filter { it.visibilitySetting == Visibility.ON }.filterNot { bindOnlyArray && it.keyBind.key[0] == Keyboard.KEY_NONE }.map { ModuleRenderInfo(it) }.filterNot { it.module.isDisabled && it.animation == 0.0 }
    private var information = CopyOnWriteArrayList<Information>()

    private var coordinatesText = "null"

    init {
        onTick {
            runSafe {
                val inHell = InfoHUD.mc.player.dimension == -1
                val posX = String.format("%.1f", InfoHUD.mc.player.posX)
                val posY = String.format("%.1f", InfoHUD.mc.player.posY)
                val posZ = String.format("%.1f", InfoHUD.mc.player.posZ)
                val f = if (!inHell) 0.125f else 8.0f
                val hellPosX = String.format("%.1f", InfoHUD.mc.player.posX * f.toDouble())
                val hellPosZ = String.format("%.1f", InfoHUD.mc.player.posZ * f.toDouble())
                val ow = "$posX, $posY, $posZ"
                val nether = "$hellPosX, $posY, $hellPosZ"

                coordinatesText = ChatUtil.SECTION_SIGN + "rXYZ " + ChatUtil.SECTION_SIGN + "f" + ow + if (netherCoords) ChatUtil.SECTION_SIGN + "r [" + ChatUtil.SECTION_SIGN + "f" + nether + ChatUtil.SECTION_SIGN + "r]" else ""
                information.clear()
                val tf = SimpleDateFormat("HH:mm:ss")
                val times = Date()
                val ip = "IP " + "\u00a7f" + if (InfoHUD.mc.isSingleplayer) "Single Player" else InfoHUD.mc.currentServerData?.serverIP?.lowercase(Locale.getDefault())
                val time = "Time " + "\u00a7f" + tf.format(times)

                //Durability calculate
                val durability = "Durability "
                val itemStack = InfoHUD.mc.player.heldItemMainhand
                val green = (itemStack.maxDamage.toFloat() - itemStack.itemDamage.toFloat()) / itemStack.maxDamage.toFloat()
                val red = 1 - green
                val dmg = itemStack.maxDamage - (red * itemStack.itemDamage).toInt()
                val color = ColorRGB((red * 255).toInt(), (green * 255).toInt(), 0)
                val speed = "Speed " + ChatUtil.SECTION_SIGN + "f" + speed() + speedUnit.standardName
                val onlinePlayer: Int = InfoHUD.mc.player.connection.playerInfoMap.toList().size
                val playerCount = "Player" + (if (onlinePlayer > 1) "s" else "") + " " + ChatUtil.SECTION_SIGN + "f" + onlinePlayer
                val privatePingValue: Int = TpsCalculator.globalInfoPingValue()
                val ping = "Ping " + ChatUtil.SECTION_SIGN + "f" + privatePingValue
                val fps = "Fps " + ChatUtil.SECTION_SIGN + "f" + Minecraft.getDebugFPS()
                val tps = "Tps " + ChatUtil.SECTION_SIGN + "f" + String.format("%.2f", TpsCalculator.tickRate)

                if (displayIp) information.add(Information(ip))
                if (displayTime) information.add(Information(time))
                if (displayDurability && dmg > 0) information.add(Information(durability, color, dmg))
                if (displaySpeed) information.add(Information(speed))
                if (countPlayer) information.add(Information(playerCount))
                if (displayTps) information.add(Information(tps))
                if (displayFps) information.add(Information(fps))
                if (displayPing) information.add(Information(ping))
            }
        }

        asyncRender {
            runCatching {
                val resolution = ScaleHelper.scaledResolution
                val fontColor = GUIManager.firstColor.alpha(255)
                val coordsX = 2f
                var coordsY = resolution.scaledHeight - MainFontRenderer.getHeight() - getUpPos() - 2.0f

                if (coordinates) {
                    drawStringWithShadow(coordinatesText, coordsX, coordsY, fontColor, 1f)
                    coordsY -= 10.0f
                }
                if (direction) {
                    drawStringWithShadow(ChatUtil.SECTION_SIGN + "f" + getCardinalDirection() + ChatUtil.SECTION_SIGN + "r [" + ChatUtil.SECTION_SIGN + "f" + getFacing() + ChatUtil.SECTION_SIGN + "r]", coordsX, coordsY, fontColor, 1f)
                }

                if (arrayList) renderModuleList(resolution, fontColor)

                var potionAdd = 0
                var count = 0
                if (potionEffect) {
                    for (effect in mc.player.activePotionEffects.toList()) {
                        val name = I18n.format(effect.potion.name).toString()
                        val amplifier = effect.amplifier + 1
                        val color = effect.potion.liquidColor or -0x1000000
                        val str = name + " " + amplifier + ChatFormatting.WHITE + " " + Potion.getPotionDurationString(effect, 1.0f)
                        val isBottom = potionPos == Position.BotRight || potionPos == Position.BotLeft
                        val potionX = getPosX(str, resolution, potionPos)
                        val potionY = getPosY(resolution, potionPos)
                        val posAdd = if (isBottom) count * -10 - getUpPos() else count * 10.toFloat()
                        drawStringWithShadow(str, potionX, potionY + posAdd, ColorRGB(ColorUtils.argbToRgba(color)), 1f)
                        count++
                        potionAdd = abs(posAdd).toInt() + 10
                    }
                }
                if (info) {
                    var stringY = 1f
                    val isSame: Boolean = infoPos == potionPos
                    if (infoPos == Position.BotRight || infoPos == Position.BotLeft) {
                        stringY = getPosY(resolution, infoPos) - (if (isSame) if (potionAdd == 0) getUpPos() else potionAdd else getUpPos()).toFloat()
                    } else {
                        if (isSame) {
                            stringY += potionAdd
                        }
                    }
                    val isRight = infoPos == Position.BotRight || infoPos == Position.TopRight
                    information.forEach { info ->
                        val stringX = getPosX(info.info, resolution, infoPos)
                        if (info.color != null) {
                            val dmg2 = info.dmg.toString()
                            drawStringWithShadow(info.info, stringX - if (isRight) MainFontRenderer.getWidth(dmg2) else 0f, stringY, fontColor, 1f)
                            drawStringWithShadow(dmg2, stringX + MainFontRenderer.getWidth(info.info) - if (isRight) MainFontRenderer.getWidth(dmg2) else 0f, stringY, info.color, 1f)
                        } else drawStringWithShadow(info.info, stringX, stringY, fontColor, 1f)
                        if (infoPos == Position.BotRight || infoPos == Position.BotLeft) {
                            stringY -= 11
                        } else {
                            stringY += 11
                        }
                    }
                }
            }.onFailure { it.printStackTrace() }
        }
    }

    private var hue = 1.0f
    private val rainbowTimer = Timer()

    private fun AsyncRenderer.renderModuleList(
        resolution: ScaledResolution,
        fontColor: ColorRGB,
    ) {
        val display = if (sort) displayModules.sortedByDescending { it.width }.toList() else displayModules.toList()
        val yIncrementMultiplier = if (listPos == ListPos.Top) 1 else -1
        val stringXOffset = if (rect == RectMode.RightTag) 4 else 2
        val bgColor = ColorRGB(13, 13, 13, alphaRect)
        val last = display.size - 1
        var posY: Double = getModuleListPosY(resolution).toDouble()
        var prevPosX = 0.0

        if (GUIManager.isRainbow) {
            if (rainbowTimer.passed(10)) {
                hue += club.eridani.epsilon.client.module.setting.GuiSetting.rainbowSpeed.value / 1000f
                rainbowTimer.reset()
            }
            if (hue > 1.0F) {
                hue = 0.0F
            }
        }

        var h = hue
        for ((index, info) in display.withIndex()) {
            val width = info.width * info.animation
            val height = listHeight * info.animation
            val posX = resolution.scaledWidth - width
            val color = if (GUIManager.isRainbow) {
                ColorRGB(ColorUtils.argbToRgba(Color.HSBtoRGB(h, GuiSetting.saturation.value, GuiSetting.brightness.value))).alpha(255)
            } else {
                fontColor
            }

            val left = posX - stringXOffset - 2.0
            val right = posX + width
            val bottom = posY + height
            if (rect != RectMode.None) drawRect(left.toFloat(), posY.toFloat(), right.toFloat(), bottom.toFloat(), bgColor)
            when (rect) {
                RectMode.RightTag -> drawRect(right.toFloat() - 2f, posY.toFloat(), right.toFloat(), bottom.toFloat(), color)
                RectMode.LeftTag -> drawRect(left.toFloat() - 2f, posY.toFloat(), left.toFloat(), bottom.toFloat(), color)
                RectMode.Sigma -> {
                    drawRect(left.toFloat() - 1f, posY.toFloat(), left.toFloat(), bottom.toFloat() + 1f, color)

                    if (index != 0) drawRect(prevPosX.toFloat() - stringXOffset - 2f, posY.toFloat(), left.toFloat() - 1.0f, posY.toFloat() + 1.0f, color)
                    if (index == last) drawRect(left.toFloat(), bottom.toFloat(), right.toFloat(), bottom.toFloat() + 1.0f, color)
                }
                else -> {
                }
            }
            drawStringWithShadow(info.string, (posX - stringXOffset).toFloat(), posY.toFloat() + (height.toFloat() / 2f) - (club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight() / 2f), color, 1.06f)

            posY += height * yIncrementMultiplier
            prevPosX = posX
            h += 0.02161f
        }
    }

    private fun getModuleListPosY(resolution: ScaledResolution): Int {
        var y: Int
        if (listPos == ListPos.Top) {
            y = 0
            if (potionLogo == PotionHUD.Down && mc.player.activePotionEffects.isNotEmpty()) {
                y += 26
            }
        } else {
            y = (resolution.scaledHeight - 12 - getUpPos()).toInt()
        }
        return y
    }

    private fun getUpPos(): Float {
        val shouldUp = mc.currentScreen is GuiChat
        if (animationTimer.passed(16)) {
            animationTimer.reset()
            animation = AnimationUtil.animate(if (shouldUp) 14.0 else 0.0, animation, 0.1)
        }
        return animation.toFloat()
    }

    private fun getPosX(string: String, resolution: ScaledResolution, mode: Position): Float {
        if (mode == Position.TopRight) {
            return resolution.scaledWidth - MainFontRenderer.getWidth(string) - 2
        }
        if (mode == Position.BotRight) {
            return resolution.scaledWidth - MainFontRenderer.getWidth(string) - 2
        }
        return if (mode == Position.TopLeft || mode == Position.BotLeft) {
            2f
        } else 0f
    }


    private fun getPosY(resolution: ScaledResolution, mode: Position): Float {
        return if (mode == Position.BotRight || mode == Position.BotLeft) resolution.scaledHeight - 11f else 0f
    }

    private fun speed(): String {
        val currentTps = mc.timer.tickLength / 1000.0f
        var multiply = 1.0
        if (speedUnit == SpeedUtil.Km) {
            multiply = 3.6 // convert mps to kmh
        }
        return formatter.format(MathHelper.sqrt(coordsDiff('x').pow(2.0) + coordsDiff('z').pow(2.0)) / currentTps * multiply)
    }

    private fun coordsDiff(s: Char): Double {
        return when (s) {
            'x' -> mc.player.posX - mc.player.prevPosX
            'z' -> mc.player.posZ - mc.player.prevPosZ
            else -> 0.0
        }
    }

    fun getModuleListString(module: AbstractModule): String {
        return module.name + if (module.getHudInfo().isNullOrEmpty()) "" else " " + ChatUtil.SECTION_SIGN + "7" + (if (module.getHudInfo().equals("") || module.getHudInfo() == null) "" else "[") + ChatUtil.SECTION_SIGN + "f" + module.getHudInfo() + ChatUtil.SECTION_SIGN + "7" + if (module.getHudInfo().equals("")) "" else "]"
    }

    private fun getFacing(): String {
        when (MathHelper.floor((Minecraft.getMinecraft().player.rotationYaw * 8.0f / 360.0f).toDouble() + 0.5) and 7) {
            0 -> return "+Z"
            1 -> return "-X +Z"
            2 -> return "-X"
            3 -> return "-X -Z"
            4 -> return "-Z"
            5 -> return "+X -Z"
            6 -> return "+X"
            7 -> return "+X +Z"
        }
        return "Invalid"
    }

    private fun getCardinalDirection(): String {
        when (MathHelper.floor((Minecraft.getMinecraft().player.rotationYaw * 8.0f / 360.0f).toDouble() + 0.5) and 7) {
            0 -> return "South"
            1 -> return "South West"
            2 -> return "West"
            3 -> return "North West"
            4 -> return "North"
            5 -> return "North East"
            6 -> return "East"
            7 -> return "South East"
        }
        return "Invalid"
    }

    @JvmInline
    value class ModuleRenderInfo(val module: AbstractModule) {
        val string: String get() = getModuleListString(module)
        val width get() = MainFontRenderer.getWidth(string, 1.06f) + 1
        val delta: Int get() = AnimationUtil.toDelta(module.toggleTime)

        val animation
            get() = if (module.isEnabled) AnimationUtil.exponentInc(delta, 200) else AnimationUtil.exponentDec(delta, 200)
    }

    class Information(val info: String, val color: ColorRGB? = null, val dmg: Int = -1)

    enum class RectMode {
        None, Rect, RightTag, LeftTag, Sigma
    }

    enum class ListPos {
        Top, Bottom
    }

    enum class PotionHUD {
        Down, Hide, Ignore
    }

    enum class Position {
        TopLeft, TopRight, BotLeft, BotRight
    }


    enum class SpeedUtil(val standardName: String) {
        Km("km/h"), Ms("m/s")
    }

}