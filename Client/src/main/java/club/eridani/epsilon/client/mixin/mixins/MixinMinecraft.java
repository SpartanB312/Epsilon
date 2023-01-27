package club.eridani.epsilon.client.mixin.mixins;

import club.eridani.epsilon.client.concurrent.MainThreadExecutor;
import club.eridani.epsilon.client.config.ConfigManager;
import club.eridani.epsilon.client.event.SafeClientEvent;
import club.eridani.epsilon.client.event.decentralized.events.client.ClientTickDecentralizedEvent;
import club.eridani.epsilon.client.event.decentralized.events.client.InputUpdateDecentralizedEvent;
import club.eridani.epsilon.client.event.decentralized.events.client.KeyDecentralizedEvent;
import club.eridani.epsilon.client.event.events.GuiEvent;
import club.eridani.epsilon.client.event.events.RunGameLoopEvent;
import club.eridani.epsilon.client.event.events.TickEvent;
import club.eridani.epsilon.client.launch.InitManager;
import club.eridani.epsilon.client.management.InputManager;
import club.eridani.epsilon.client.menu.main.MainMenu;
import club.eridani.epsilon.client.module.combat.AutoClicker;
import club.eridani.epsilon.client.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = Integer.MAX_VALUE - 312)
public class MixinMinecraft {

    @Shadow
    public GuiScreen currentScreen;
    @Shadow
    public boolean skipRenderWorld;
    @Shadow
    private int leftClickCounter;

    @Inject(method = "run", at = @At("HEAD"))
    private void init(CallbackInfo callbackInfo) {
        if (Minecraft.getMinecraft().displayWidth < 1067)
            Minecraft.getMinecraft().displayWidth = 1067;

        if (Minecraft.getMinecraft().displayHeight < 622)
            Minecraft.getMinecraft().displayHeight = 622;
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void clickMouse(CallbackInfo callbackInfo) {
        if (AutoClicker.INSTANCE.isEnabled())
            leftClickCounter = 0;
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void onMinecraftInitHead(CallbackInfo ci) {
        InitManager.onMinecraftInit();
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onMinecraftInitReturn(CallbackInfo ci) {
        InitManager.onFinishingInit();
    }


    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
    public void onUnload(WorldClient worldClientIn, String loadingMessage, CallbackInfo info) {
        /*
        if (worldClientIn != null) {
            if (worldClientIn.isRemote) worldClientIn.removeEventListener(WorldManager.INSTANCE);
            WorldEvent.Unload.INSTANCE.post();
        }
         */
    }

    @Inject(method = "getLimitFramerate", at = @At("HEAD"), cancellable = true)
    public void getLimitFramerate$Inject$HEAD(CallbackInfoReturnable<Integer> cir) {
        if (Wrapper.getMc().world == null && Wrapper.getMc().currentScreen != null) {
            cir.setReturnValue(60);
        }
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Timer;updateTimer()V", shift = At.Shift.BEFORE))
    public void runGameLoop$Inject$INVOKE$updateTimer(CallbackInfo ci) {
        SafeClientEvent.Companion.update();
        Wrapper.getMc().profiler.startSection("epsilonRunGameLoop");
        RunGameLoopEvent.Start.INSTANCE.post();
        Wrapper.getMc().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0, shift = At.Shift.AFTER))
    public void runGameLoop$INVOKE$endSection(CallbackInfo ci) {
        SafeClientEvent.Companion.update();
        Wrapper.getMc().profiler.startSection("epsilonRunGameLoop");
        RunGameLoopEvent.Tick.INSTANCE.post();
        Wrapper.getMc().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE))
    public void runGameLoop$Inject$INVOKE$endStartSection(CallbackInfo ci) {
        SafeClientEvent.Companion.update();
        Wrapper.getMc().profiler.endStartSection("epsilonRunGameLoop");
        RunGameLoopEvent.Render.INSTANCE.post();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isFramerateLimitBelowMax()Z", shift = At.Shift.BEFORE))
    public void runGameLoop$Inject$INVOKE$isFramerateLimitBelowMax(CallbackInfo ci) {
        SafeClientEvent.Companion.update();
        Wrapper.getMc().profiler.startSection("epsilonRunGameLoop");
        RunGameLoopEvent.End.INSTANCE.post();
        Wrapper.getMc().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private void onRunningGameLoopHead(CallbackInfo ci) {
        SafeClientEvent.Companion.update();
        MainThreadExecutor.INSTANCE.runJobs();
    }

    @Inject(method = "runGameLoop", at = @At("RETURN"))
    private void onRunningGameLoopReturn(CallbackInfo ci) {
        MainThreadExecutor.INSTANCE.runJobs();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTickPre(CallbackInfo ci) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().world != null) {
            TickEvent.Pre.INSTANCE.post();
        }
        MainThreadExecutor.INSTANCE.runJobs();
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void onRunTick(CallbackInfo ci) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().world != null) {
            ClientTickDecentralizedEvent.INSTANCE.post();
            TickEvent.INSTANCE.post();
        }
        MainThreadExecutor.INSTANCE.runJobs();
    }

    @Inject(method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void onPreInit(CallbackInfo ci) {
        InitManager.preInitHook();
    }

    @Inject(method = "init",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V",
                    ordinal = 2,
                    shift = At.Shift.AFTER
            )
    )
    private void onPostInit(CallbackInfo ci) {
        InitManager.postInitHook();
    }


    @Inject(method = "displayGuiScreen", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;currentScreen:Lnet/minecraft/client/gui/GuiScreen;", shift = At.Shift.AFTER))
    private void displayGuiScreen(CallbackInfo callbackInfo) {
//        if (MenuSetting.INSTANCE.isEnabled()) {
            if (currentScreen instanceof net.minecraft.client.gui.GuiMainMenu || (currentScreen != null && currentScreen.getClass().getName().startsWith("net.labymod") && currentScreen.getClass().getSimpleName().equals("ModGuiMainMenu"))) {
                currentScreen = MainMenu.INSTANCE;

                ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
                currentScreen.setWorldAndResolution(Minecraft.getMinecraft(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
                skipRenderWorld = false;
            }
//        }
    }

    @ModifyVariable(method = "displayGuiScreen", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public GuiScreen displayGuiScreen$ModifyVariable$HEAD(GuiScreen value) {
        GuiScreen current = this.currentScreen;
        if (current != null) {
            GuiEvent.Closed closed = new GuiEvent.Closed(current);
            closed.post();
        }

        GuiEvent.Displayed displayed = new GuiEvent.Displayed(value);
        displayed.post();
        return displayed.getScreen();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo info) {
        ConfigManager.INSTANCE.saveAll(true);
    }

    @Inject(method = "runTickKeyboard", at = @At(value = "INVOKE_ASSIGN", target = "org/lwjgl/input/Keyboard.getEventKeyState()Z", remap = false))
    private void onKeyEvent(CallbackInfo ci) {
        if (currentScreen != null)
            return;

        boolean down = Keyboard.getEventKeyState();
        int key = Keyboard.getEventKey();
        char ch = Keyboard.getEventCharacter();

        //Prevent from toggling all keys, when switching languages.
        if (key != Keyboard.KEY_NONE) {
            if (down) {
                KeyDecentralizedEvent.INSTANCE.post(new KeyDecentralizedEvent.KeyEventData(key, ch));
                InputManager.onKey(key);
            } else {
                InputUpdateDecentralizedEvent.INSTANCE.post(new InputUpdateDecentralizedEvent.InputUpdateEventData(key, ch));
            }
        }

    }

}