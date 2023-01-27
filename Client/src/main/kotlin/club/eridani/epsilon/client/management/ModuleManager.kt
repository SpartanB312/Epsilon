package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.Epsilon
import club.eridani.epsilon.client.common.AbstractModule
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.EventBus
import club.eridani.epsilon.client.gui.def.DefaultHUDEditorScreen
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.hud.combat.HoleOverlay
import club.eridani.epsilon.client.hud.combat.ObsidianWarning
import club.eridani.epsilon.client.hud.combat.TargetHud
import club.eridani.epsilon.client.hud.info.*
import club.eridani.epsilon.client.hud.spartan.EnergyShield
import club.eridani.epsilon.client.module.client.*
import club.eridani.epsilon.client.module.combat.*
import club.eridani.epsilon.client.module.misc.*
import club.eridani.epsilon.client.module.movement.*
import club.eridani.epsilon.client.module.player.*
import club.eridani.epsilon.client.module.render.*
import club.eridani.epsilon.client.module.setting.*
import club.eridani.epsilon.client.sha1
import club.eridani.epsilon.client.sha256
import club.eridani.epsilon.client.sha512
import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.onRender2D
import kotlinx.coroutines.runBlocking

@Suppress("NOTHING_TO_INLINE")
object ModuleManager : Helper {

    val modules = mutableListOf<AbstractModule>()
    val hudModules = mutableListOf<HUDModule>()
    private val hardwareID = (System.getenv("COMPUTERNAME") + System.getenv("HOMEDRIVE") + System.getProperty("os.name") + System.getProperty("os.arch") + System.getProperty("os.version") + Runtime.getRuntime().availableProcessors() + System.getenv("PROCESSOR_LEVEL") + System.getenv("PROCESSOR_REVISION") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_ARCHITECTURE") + System.getenv("PROCESSOR_ARCHITEW6432") + System.getenv("NUMBER_OF_PROCESSORS")).sha1().sha256().sha512().sha1().sha256()

    init {
        if (Epsilon.authClient!!.receivedMessage == hardwareID.sha1()) {
            runBlocking {
                hudModules.forEach {
                    it.name = "ZealotCrystal"
                }
                modules.forEach {
                    it.name = "ZealotCrystal"
                }

                runBlocking {
                    Wrapper.mc.world = null
                    Wrapper.mc.player = null
                }


                EventBus.registered.clear()
                EventBus.registeredParallel.clear()
                EventBus.subscribed.clear()
                EventBus.subscribedParallel.clear()
            }
        }

        runCatching {
            Class.forName("a.g")
            CombatSetting.register()
            FontSetting.register()
            GuiSetting.register()
            MenuSetting.register()
            TextSetting.register()
            ThemeSetting.register()
        }.onFailure {

        }

        //Client
        DiscordPresence.register()
        HUDEditor.register()
        InfoHUD.register()
        NotificationRender.register()
        RootGUI.register()

        runCatching {
            Class.forName("a.x")
            //Combat
            AimAssist.register()
            AimBot.register()
            AntiAntiBurrow.register()
            AntiCev.register()
            AnvilCity.register()
            AutoBurrow.register()
            AutoCev.register()
            AutoCity.register()
            AutoClicker.register()
            ZealotCrystalTwo.register()
            AutoHoleFill.register()
            AutoLog.register()
            AutoMend.register()
            AutoOffhand.register()
            AutoTotem.register()
            AutoTrap.register()
            BedAura.register()
            Burrow.register()
            Critical.register()
            HoleSnap.register()
            KillAura.register()
            Surround.register()
            TargetStrafe.register()
            TotemPopCounter.register()
        }.onFailure {

        }
        ZealotCrystalPlus.register()

        //Misc
        AntiBot.register()
        AntiCrasher.register()
        AntiWeather.register()
        AutoFish.register()
//        AutoObsidian.register()
        AutoPorn.register()
        AutoReconnect.register()
        AutoRespawn.register()
        AutoTool.register()
        BowMcBomb.register()
        ClientSpoof.register()
        Crasher.register()
        FakePlayer.register()
        MiddleClick.register()
        MountBypass.register()
        NoRotate.register()
        PingSpoof.register()
        Refill.register()
        SkinBlinker.register()
        XCarry.register()

        //Movement
        AntiHunger.register()
//        AntiLevitation.register()
        AntiWeb.register()
        AutoCenter.register()
        AutoJump.register()
        AutoRemount.register()
        AutoWalk.register()
        ElytraFlight.register()
        ElytraReplace.register()
        EntitySpeed.register()
        FastSwim.register()
        Flight.register()
        InstantDrop.register()
        InventoryMove.register()
        Jesus.register()
        LongJump.register()
        NoFall.register()
        NoSlowDown.register()
        SafeWalk.register()
        Scaffold.register()
        Speed.register()
        Sprint.register()
        Step.register()
//        Strafe.register()
        Velocity.register()

        //Player
        AntiAim.register()
        AutoArmour.register()
        Freecam.register()
        Hitbox.register()
        LagBackCheck.register()
        LiquidInteract.register()
        NoVoid.register()
        PacketMine.register()
        Reach.register()
        WTap.register()

        //Render
        Animations.register()
        AntiOverlay.register()
        BreakESP.register()
        CameraClip.register()
        Chams.register()
        ChinaHat.register()
        CityESP.register()
        Crosshair.register()
        EntityESP.register()
        ESP2D.register()
        FullBright.register()
        HealthParticle.register()
        HoleESP.register()
        ItemESP.register()
        Nametags.register()
        NoRender.register()
        Skeleton.register()
        TextPopper.register()
        Tracers.register()
        Trajectories.register()
        ViewModel.register()
        WallHack.register()

        //CombatHUD
        HoleOverlay.register()
        ObsidianWarning.register()
        TargetHud.register()

        //InfoHUD
        CombatInfo.register()
        Compass.register()
        Inventory.register()
        Keystroke.register()
        LagNotification.register()
        Logo.register()
        Welcomer.register()

        //Spartan
        EnergyShield.register()


        modules.sortBy { it.name }
        hudModules.sortBy { it.name }

        onRender2D {
            DefaultHUDEditorScreen.hudList.reversed().forEach {
                if (!HUDEditor.isHUDEditor() && it.hudModule.isEnabled) it.hudModule.onRender()
            }
        }



        runCatching {
            Class.forName("a.c")
        }.onFailure {
            hudModules.forEach {
                it.name = "ZealotCrystal"
            }
            modules.forEach {
                it.name = "ZealotCrystal"
            }

            runBlocking {
                Wrapper.mc.world = null
                Wrapper.mc.player = null
            }


            EventBus.registered.clear()
            EventBus.registeredParallel.clear()
            EventBus.subscribed.clear()
            EventBus.subscribedParallel.clear()
        }
    }

    fun getModulesByCategory(category: Category): List<AbstractModule> {
        return modules.asSequence().filter {
            it.category == category
        }.toList()
    }

    private inline fun AbstractModule.register() {
        registerNewModule(this)
    }

    private inline fun registerNewModule(abstractModule: AbstractModule) {
        modules.add(abstractModule)
        if (abstractModule.category.isHUD) {
            hudModules.add(abstractModule as HUDModule)
        }
    }

}