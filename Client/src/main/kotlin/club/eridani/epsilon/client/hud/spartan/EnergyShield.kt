package club.eridani.epsilon.client.hud.spartan

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.VertexHelper
import club.eridani.epsilon.client.util.math.Vec2d
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.*

object EnergyShield : HUDModule(
    name = "EnergyShield",
    category = Category.SpartanHUD,
    description = "Spartans never die"
) {

    private val mode by setting("Mode", Mode.Halo3)
    private val scale by setting("Scale", 1.0, 0.5..2.0, 0.05)

    private var lastHealth = 0.0
    private var lastAbsorption = 0.0
    private var abAlphaRate = 0.0
    private var healthLineAlphaRate = 0.0

    private val updateTimer = Timer()

    private val halo3abPoses = mutableListOf<Vec2d>()
    private val halo3healthPoses = mutableListOf<Vec2d>()

    private val infiniteAbPoses = mutableListOf<Vec2d>()

    private val reachAbPoses = mutableListOf<Vec2d>()
    private val reachHealthLeftPoses = mutableListOf<Vec2d>()
    private val reachHealthRightPoses = mutableListOf<Vec2d>()
    private val reachHealthBoxLeftPoses = mutableListOf<Vec2d>()
    private val reachHealthBoxRightPoses = mutableListOf<Vec2d>()

    private val reachHealthCenterPoses = mutableListOf<Vec2d>()

    init {
        initHalo3()
        initInfinite()
        initReach()
    }

    override fun onRender() {
        if (Utils.nullCheck()) return
        resize {
            width = (scale * 320).toInt()
            height = (scale * 320).toInt() / 5
        }


        if (updateTimer.passed(16)) {
            lastHealth = AnimationUtil.animate(Wrapper.mc.player?.health?.toDouble() ?: 20.0, lastHealth, 0.1)
            lastAbsorption =
                AnimationUtil.animate(Wrapper.mc.player?.absorptionAmount?.toDouble() ?: 16.0, lastAbsorption, 0.1)
            abAlphaRate =
                AnimationUtil.animate(
                    if ((Wrapper.mc.player?.absorptionAmount ?: 16f) > 0f) 1.0 else 0.0,
                    abAlphaRate,
                    0.005
                )
            healthLineAlphaRate =
                AnimationUtil.animate(if (abAlphaRate == 0.0) 1.0 else 0.0, healthLineAlphaRate, 0.005)
            updateTimer.reset()
        }

        val healthRate = MathHelper.clamp(lastHealth / 20.0, 0.0, 1.0)
        val absorptionRate = MathHelper.clamp(lastAbsorption / 16.0, 0.0, 1.0)

        val cachedScale = scale

        glScaled(cachedScale, cachedScale, cachedScale)
        when (mode) {
            Mode.Halo3 -> renderHalo3(healthRate, absorptionRate, cachedScale)
            Mode.Infinite -> renderInfinite(healthRate, absorptionRate, cachedScale)
            Mode.Reach -> renderReach(healthRate, absorptionRate, cachedScale)
        }
        glScaled(1.0 / cachedScale, 1.0 / cachedScale, 1.0 / cachedScale)

    }

    private fun renderReach(healthRate: Double, absorptionRate: Double, scale: Double) {
        val abPoses = offsetPoses(reachAbPoses, this.x.toDouble() / scale, this.y.toDouble() / scale)

        val abWidth = (160 * absorptionRate).toInt()
        //Left 48
        drawPolygon(
            abPoses,
            GUIManager.firstColor.alpha((128 * abAlphaRate).toInt()),
            (x / scale).toInt() + 160 - abWidth,
            (x / scale).toInt() + 48
        )

        //Mid 223
        drawPolygon(
            abPoses,
            GUIManager.firstColor.alpha((128 * abAlphaRate).toInt()),
            (x / scale).toInt() + MathHelper.clamp(160 - abWidth, 48, 160),
            (x / scale).toInt() + MathHelper.clamp(160 + abWidth, 160, 271)
        )

        //Right 48
        drawPolygon(
            abPoses,
            GUIManager.firstColor.alpha((128 * abAlphaRate).toInt()),
            (x / scale).toInt() + 271,
            (x / scale).toInt() + 160 + abWidth
        )

        val healthWidth = (124 * healthRate).toInt()

        reachHealthBoxLeftPoses.forEach {
            drawReachHealthRect(
                this.x.toDouble() / scale + it.x,
                this.y.toDouble() / scale + it.y,
                (x / scale).toInt() + 160 - healthWidth,
                (x / scale).toInt() + 160 + healthWidth,
                true
            )
        }
        reachHealthBoxRightPoses.forEach {
            drawReachHealthRect(
                this.x.toDouble() / scale + it.x - 12,
                this.y.toDouble() / scale + it.y,
                (x / scale).toInt() + 160 - healthWidth,
                (x / scale).toInt() + 160 + healthWidth,
                false
            )
        }

        val centerPoses = offsetPoses(reachHealthCenterPoses, this.x.toDouble() / scale, this.y.toDouble() / scale)
        drawPolygon(
            centerPoses,
            GUIManager.firstColor.alpha(128),
            (x / scale).toInt() + 160 - healthWidth,
            (x / scale).toInt() + 160 + healthWidth,
        )
        drawLines(centerPoses, GUIManager.firstColor.alpha(255), 2.0F)

        drawLines(abPoses, GUIManager.firstColor.alpha((255 * abAlphaRate).toInt()), 2.0F)
    }

    private fun drawReachHealthRect(startX: Double, startY: Double, drawMin: Int, drawMax: Int, left: Boolean) {
        val poses = offsetPoses(if (left) reachHealthLeftPoses else reachHealthRightPoses, startX, startY)

        drawPolygon(
            poses,
            GUIManager.firstColor.alpha(128),
            drawMin,
            drawMax
        )

        drawLines(poses, GUIManager.firstColor.alpha(255), 2.0F)
    }

    private fun renderHalo3(healthRate: Double, absorptionRate: Double, scale: Double) {
        val abPoses = offsetPoses(halo3abPoses, this.x.toDouble() / scale, this.y.toDouble() / scale)
        val healthPoses = offsetPoses(halo3healthPoses, this.x.toDouble() / scale, this.y.toDouble() / scale)
        drawPolygon(
            abPoses,
            GUIManager.firstColor.alpha((128 * abAlphaRate).toInt()),
            Int.MIN_VALUE,
            (x / scale + (1 + 5 + 310 * absorptionRate)).toInt()
        )

        drawPolygon(
            healthPoses,
            GUIManager.firstColor.alpha(255),
            Int.MIN_VALUE,
            (x / scale + (1 + 35 + 250 * healthRate)).toInt()
        )

        drawLines(healthPoses, GUIManager.firstColor.alpha((255 * healthLineAlphaRate).toInt()), 1.0F)
        drawLines(abPoses, GUIManager.firstColor.alpha((255 * abAlphaRate).toInt()), 2.0F)
    }

    private fun renderInfinite(healthRate: Double, absorptionRate: Double, scale: Double) {
        val abPoses = offsetPoses(infiniteAbPoses, this.x.toDouble() / scale, this.y.toDouble() / scale)
        drawPolygon(
            abPoses,
            GUIManager.firstColor.alpha(128),
            (x / scale + (1 + 320 * absorptionRate)).toInt()
        )

        drawLines(abPoses, GUIManager.firstColor, 2.0F)
    }

    private fun initReach() {
        for (x in 0..267) {
            reachAbPoses.add(Vec2d(26.0 + x, 0.0))
        }
        for (x in 1..26) {
            reachAbPoses.add(Vec2d(293.0 + x, 0.0 + x))
        }
        for (x in 1..36) {
            reachAbPoses.add(Vec2d(319.0 - x, 26.0))
        }
        for (x in 1..12) {
            reachAbPoses.add(Vec2d(283.0 - x, 26.0 - x))
        }
        for (x in 1..223) {
            reachAbPoses.add(Vec2d(271.0 - x, 14.0))
        }
        for (x in 1..12) {
            reachAbPoses.add(Vec2d(48.0 - x, 14.0 + x))
        }
        for (x in 1..36) {
            reachAbPoses.add(Vec2d(36.0 - x, 26.0))
        }
        for (x in 1..26) {
            reachAbPoses.add(Vec2d(0.0 + x, 26.0 - x))
        }

        //Left
        for (x in 0..11) {
            reachHealthLeftPoses.add(Vec2d(x.toDouble(), 0.0))
        }
        for (x in 1..10) {
            reachHealthLeftPoses.add(Vec2d(12.0 - x, x.toDouble()))
        }
        for (x in 1..12) {
            reachHealthLeftPoses.add(Vec2d(2.0 - x, 10.0))
        }
        for (x in 1..10) {
            reachHealthLeftPoses.add(Vec2d(-10.0 + x, 10.0 - x))
        }

        //Right
        for (x in 0..12) {
            reachHealthRightPoses.add(Vec2d(x.toDouble(), 0.0))
        }
        for (x in 1..10) {
            reachHealthRightPoses.add(Vec2d(12.0 + x, x.toDouble()))
        }
        for (x in 1..12) {
            reachHealthRightPoses.add(Vec2d(22.0 - x, 10.0))
        }
        for (x in 1..10) {
            reachHealthRightPoses.add(Vec2d(10.0 - x, 10.0 - x))
        }

        reachHealthBoxLeftPoses.add(Vec2d(50.0, 16.0))
        reachHealthBoxLeftPoses.add(Vec2d(64.0, 16.0))
        reachHealthBoxLeftPoses.add(Vec2d(78.0, 16.0))
        reachHealthBoxLeftPoses.add(Vec2d(92.0, 16.0))
        reachHealthBoxLeftPoses.add(Vec2d(106.0, 16.0))
        reachHealthBoxLeftPoses.add(Vec2d(120.0, 16.0))
        reachHealthBoxLeftPoses.add(Vec2d(134.0, 16.0))

        reachHealthBoxRightPoses.add(Vec2d(269.0, 16.0))
        reachHealthBoxRightPoses.add(Vec2d(255.0, 16.0))
        reachHealthBoxRightPoses.add(Vec2d(241.0, 16.0))
        reachHealthBoxRightPoses.add(Vec2d(227.0, 16.0))
        reachHealthBoxRightPoses.add(Vec2d(213.0, 16.0))
        reachHealthBoxRightPoses.add(Vec2d(199.0, 16.0))
        reachHealthBoxRightPoses.add(Vec2d(185.0, 16.0))

        for (x in 148..171) {
            reachHealthCenterPoses.add(Vec2d(x.toDouble(), 16.0))
        }
        for (x in 1..10) {
            reachHealthCenterPoses.add(Vec2d(171.0 + x.toDouble(), 16.0 + x.toDouble()))
        }
        for (x in 181 downTo 138) {
            reachHealthCenterPoses.add(Vec2d(x.toDouble(), 26.0))
        }
        for (x in 1..10) {
            reachHealthCenterPoses.add(Vec2d(138.0 + x.toDouble(), 26.0 - x.toDouble()))
        }

    }

    private fun initInfinite() {
        val posesUp = mutableListOf<Vec2d>()
        for (x in 0..290) {
            posesUp.add(Vec2d(x.toDouble(), -((x - 145) * (x - 145) * 0.0003)))
        }
        val posesDown = mutableListOf<Vec2d>()
        for (x in 0..296) {
            posesDown.add(Vec2d(x.toDouble(), -((x - 148) * (x - 148) * 0.0002)))
        }
        infiniteAbPoses.addAll(offsetPoses(posesUp, 15.0, 10.0).reversed())
        infiniteAbPoses.add(Vec2d(8.0, 14.0))
        infiniteAbPoses.addAll(offsetPoses(posesDown, 12.0, 25.0))
        infiniteAbPoses.add(Vec2d(312.0, 14.0))
    }

    private fun initHalo3() {
        for (x in 20..300) {
            halo3abPoses.add(Vec2d(x.toDouble(), 5.0))
        }
        for (offset in 1..15) {
            halo3abPoses.add(Vec2d(300.0 + offset, 5.0 + offset))
        }
        for (offset in 0..5) {
            halo3abPoses.add(Vec2d(315.0 - offset, 20.0 + offset))
        }
        for (x in 310 downTo 10) {
            halo3abPoses.add(Vec2d(x.toDouble(), 25.0))
        }
        for (offset in 0..5) {
            halo3abPoses.add(Vec2d(10.0 - offset, 25.0 - offset))
        }
        for (offset in 1..15) {
            halo3abPoses.add(Vec2d(5.0 + offset, 20.0 - offset))
        }
        for (x in 42..278) {
            halo3healthPoses.add(Vec2d(x.toDouble(), 18.0))
        }
        for (offset in 1..7) {
            halo3healthPoses.add(Vec2d(278.0 + offset, 18.0 + offset))
        }
        for (x in 285 downTo 35) {
            halo3healthPoses.add(Vec2d(x.toDouble(), 25.0))
        }
        for (offset in 1..7) {
            halo3healthPoses.add(Vec2d(35.0 + offset, 25.0 - offset))
        }
    }

    private fun offsetPoses(poses: List<Vec2d>, startX: Double, startY: Double): List<Vec2d> {
        val newList = mutableListOf<Vec2d>()
        poses.forEach {
            newList.add(Vec2d(it.x + startX, it.y + startY))
        }
        return newList
    }

    private fun drawPolygon(
        poses: List<Vec2d>,
        color: ColorRGB,
        drawMin: Int = Int.MIN_VALUE,
        drawMax: Int = Int.MAX_VALUE,
    ) {
        RenderUtils2D.prepareGl()
        glEnable(GL_POLYGON_SMOOTH)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)

        VertexHelper.begin(GL_POLYGON)
        for (pos in poses) {
            if (pos.x > drawMax || pos.x < drawMin) continue
            VertexHelper.put(pos.x, pos.y, color)
        }
        VertexHelper.end()

        glDisable(GL_POLYGON_SMOOTH)
        RenderUtils2D.releaseGl()
    }

    private fun drawLines(poses: List<Vec2d>, color: ColorRGB, lineWidth: Float = 1.0F) {
        RenderUtils2D.prepareGl()
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        VertexHelper.begin(GL_LINES)
        for (index in poses.indices) {
            if (index != poses.size - 1) {
                VertexHelper.put(poses[index].x, poses[index].y, color)
                VertexHelper.put(poses[index + 1].x, poses[index + 1].y, color)
            } else {
                VertexHelper.put(poses[index].x, poses[index].y, color)
                VertexHelper.put(poses[0].x, poses[0].y, color)
            }
        }
        VertexHelper.end()

        glDisable(GL_LINE_SMOOTH)
        glLineWidth(1.0F)
        RenderUtils2D.releaseGl()
    }

    enum class Mode {
        Halo3,
        Infinite,
        Reach,
    }
}