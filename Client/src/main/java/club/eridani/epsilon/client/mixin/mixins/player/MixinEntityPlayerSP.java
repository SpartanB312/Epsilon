package club.eridani.epsilon.client.mixin.mixins.player;

import club.eridani.epsilon.client.event.EventBus;
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent;
import club.eridani.epsilon.client.event.events.PlayerInputEvent;
import club.eridani.epsilon.client.event.events.PlayerMoveEvent;
import club.eridani.epsilon.client.event.events.PlayerPushEvent;
import club.eridani.epsilon.client.management.PlayerPacketManager;
import club.eridani.epsilon.client.module.movement.Scaffold;
import club.eridani.epsilon.client.module.movement.Sprint;
import club.eridani.epsilon.client.module.player.AntiAim;
import club.eridani.epsilon.client.util.Wrapper;
import club.eridani.epsilon.client.util.math.Vec2f;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityPlayerSP.class, priority = 10001)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    @Shadow
    @Final
    public NetHandlerPlayClient connection;
    @Shadow
    private boolean serverSneakState;
    @Shadow
    private boolean prevOnGround;
    @Shadow
    private int positionUpdateTicks;
    @Shadow
    private double lastReportedPosX;
    @Shadow
    private double lastReportedPosY;
    @Shadow
    private double lastReportedPosZ;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    private boolean autoJumpEnabled;
    @Shadow
    protected Minecraft mc;

    @Shadow
    public final MovementInput movementInput;

    @Shadow
    protected abstract void updateAutoJump(float p_189810_1_, float p_189810_2_);

    public MixinEntityPlayerSP(World p_i45074_1_, GameProfile p_i45074_2_, MovementInput movementInput) {
        super(p_i45074_1_, p_i45074_2_);
        this.movementInput = movementInput;
    }

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @ModifyArg(method = "setSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;setSprinting(Z)V"), index = 0)
    public boolean modifySprinting(boolean sprinting) {
        if (Scaffold.INSTANCE.isEnabled() && Scaffold.INSTANCE.getMode() == Scaffold.Mode.Hypixel) return false;
        return Sprint.INSTANCE.getSprinting() || sprinting;
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void onPushOutOfBlocksPre(double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
        PlayerPushEvent event = new PlayerPushEvent(PlayerPushEvent.Type.BLOCK);
        EventBus.post(event);
        if (event.getCancelled()) ci.setReturnValue(false);
    }


    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/MovementInput;updatePlayerMoveState()V", shift = At.Shift.AFTER))
    private void onInput(CallbackInfo ci) {
        PlayerInputEvent event = new PlayerInputEvent(this, movementInput);
        EventBus.post(event);
    }

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V", shift = At.Shift.AFTER))
    private void onUpdateInvokeOnUpdateWalkingPlayer(CallbackInfo ci) {
        Vec3d serverSidePos = PlayerPacketManager.INSTANCE.getPosition();
        Vec2f serverSideRotation = PlayerPacketManager.INSTANCE.getRotation();

        this.lastReportedPosX = serverSidePos.x;
        this.lastReportedPosY = serverSidePos.y;
        this.lastReportedPosZ = serverSidePos.z;

        this.lastReportedYaw = serverSideRotation.getX();
        this.lastReportedPitch = serverSideRotation.getY();

        if (!AntiAim.INSTANCE.isEnabled() || AntiAim.INSTANCE.getServerSide()) {
            this.rotationYawHead = PlayerPacketManager.INSTANCE.getRotation().getX();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateWalkingPlayerHead(CallbackInfo ci) {
        // Setup flags
        Vec3d position = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        Vec2f rotation = new Vec2f(this.rotationYaw, this.rotationPitch);
        boolean onGround = this.onGround;

        OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData data =
                new OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData(position, rotation, onGround, OnUpdateWalkingPlayerDecentralizedEvent.Pre.INSTANCE);
        OnUpdateWalkingPlayerDecentralizedEvent.Pre.INSTANCE.post(data);

        PlayerPacketManager.INSTANCE.applyPacket(data);

        if (data.getCancelled()) {
            ci.cancel();

            if (!data.getCancelAll()) {
                // Copy flags from event
                position = data.getPosition();
                rotation = data.getRotation();
                onGround = data.isOnGround();

                boolean moving = !data.getCancelMove() && isMoving(position);
                boolean rotating = !data.getCancelRotate() && isRotating(rotation);

                sendSprintPacket();
                sendSneakPacket();
                sendPlayerPacket(moving, rotating, position, rotation, onGround);

                this.prevOnGround = onGround;
            }

            ++this.positionUpdateTicks;
            this.autoJumpEnabled = this.mc.gameSettings.autoJump;
        }


        OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData postData = new OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData(position, rotation, onGround, OnUpdateWalkingPlayerDecentralizedEvent.Post.INSTANCE);
        OnUpdateWalkingPlayerDecentralizedEvent.Post.INSTANCE.post(postData);
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    public void move$Inject$HEAD(MoverType type, double x, double y, double z, CallbackInfo ci) {
        switch (type) {
            case SELF: {
                EntityPlayerSP player = Wrapper.getPlayer();
                if (player == null) return;

                PlayerMoveEvent.Pre event = new PlayerMoveEvent.Pre(player);
                event.post();

                if (event.isModified()) {
                    double prevX = this.posX;
                    double prevZ = this.posZ;

                    super.move(type, event.getX(), event.getY(), event.getZ());
                    this.updateAutoJump((float) (this.posX - prevX), (float) (this.posZ - prevZ));
                    PlayerMoveEvent.Post.INSTANCE.post();

                    ci.cancel();
                }
            }
            case PLAYER: {
                break;
            }
            default: {
//                if (AntiAntiBurrow.INSTANCE.isEnabled() || Velocity.shouldCancelMove()) {
//                    ci.cancel();
//                }
            }
        }
    }

    @Inject(method = "move", at = @At("RETURN"), cancellable = true)
    public void move$Inject$RETURN(MoverType type, double x, double y, double z, CallbackInfo ci) {
        PlayerMoveEvent.Post.INSTANCE.post();
    }


    private void sendSprintPacket() {
        boolean sprinting = this.isSprinting();

        if (sprinting != this.serverSneakState) {
            if (sprinting) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
            }
            this.serverSneakState = sprinting;
        }
    }

    private void sendSneakPacket() {
        boolean sneaking = this.isSneaking();

        if (sneaking != this.serverSneakState) {
            if (sneaking) {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            this.serverSneakState = sneaking;
        }
    }

    private void sendPlayerPacket(boolean moving, boolean rotating, Vec3d position, Vec2f rotation, boolean onGround) {
        if (!this.isCurrentViewEntity()) return;

        if (this.isRiding()) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, rotation.getX(), rotation.getY(), onGround));
            moving = false;
        } else if (moving && rotating) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(position.x, position.y, position.z, rotation.getX(), rotation.getY(), onGround));
        } else if (moving) {
            this.connection.sendPacket(new CPacketPlayer.Position(position.x, position.y, position.z, onGround));
        } else if (rotating) {
            this.connection.sendPacket(new CPacketPlayer.Rotation(rotation.getX(), rotation.getY(), onGround));
        } else if (this.prevOnGround != onGround) {
            this.connection.sendPacket(new CPacketPlayer(onGround));
        }

        if (moving) {
            this.positionUpdateTicks = 0;
        }
    }

    private boolean isMoving(Vec3d position) {
        double xDiff = position.x - this.lastReportedPosX;
        double yDiff = position.y - this.lastReportedPosY;
        double zDiff = position.z - this.lastReportedPosZ;

        return this.positionUpdateTicks >= 20 || xDiff * xDiff + yDiff * yDiff + zDiff * zDiff > 9.0E-4D;
    }

    private boolean isRotating(Vec2f rotation) {
        double yawDiff = rotation.getX() - this.lastReportedYaw;
        double pitchDiff = rotation.getY() - this.lastReportedPitch;

        return yawDiff != 0.0D || pitchDiff != 0.0D;
    }

}
