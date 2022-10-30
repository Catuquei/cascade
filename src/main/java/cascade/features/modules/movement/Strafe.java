package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.*;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.Timer;
import cascade.util.player.HoleUtil;
import cascade.util.player.MovementUtil;
import cascade.util.player.PhysicsUtil;
import cascade.util.player.PlayerUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Strafe extends Module {

    public Strafe() {
        super("Strafe", Category.MOVEMENT, "Lets u go fast as fuck");
        INSTANCE = this;
    }

    Setting<Page> page = register(new Setting("Page", Page.Normal));
    enum Page {Normal, Knockback, TickBoost}

    Setting<Boolean> noLiquid = register(new Setting("NoLiquid", true, v -> page.getValue() == Page.Normal));
    Setting<Sneaking> sneaking = register(new Setting("Sneaking", Sneaking.Cancel, v -> page.getValue() == Page.Normal));
    enum Sneaking{None, Pause, Cancel}
    Setting<Boolean> step = register(new Setting("Step", true, v -> page.getValue() == Page.Normal));
    Setting<Float> height = register(new Setting("Height", 1.9f, 0.1f, 2.5f, v -> page.getValue() == Page.Normal && step.getValue()));

    Setting<Boolean> kbBoost = register(new Setting("KbBoost", true, v -> page.getValue() == Page.Knockback));
    Setting<Boolean> kbStep = register(new Setting("StepOnBoost", true, v -> page.getValue() == Page.Knockback && kbBoost.getValue()));
    Setting<Float> kbHeight = register(new Setting("StepHeight", 2.0f, 0.1f, 2.5f, v -> page.getValue() == Page.Knockback && kbBoost.getValue() && kbStep.getValue()));
    Setting<Double> kbFactor = register(new Setting("KbFactor", 6.0d, 0.1d, 12.0d, v -> page.getValue() == Page.Knockback && kbBoost.getValue()));

    Setting<Boolean> tickBoost = register(new Setting("TickBoost", true, v -> page.getValue() == Page.TickBoost));
    Setting<Integer> tickFactor = register(new Setting("TickFactor", 4, 1, 32, v -> page.getValue() == Page.TickBoost && tickBoost.getValue()));
    Setting<Boolean> doubleTap = register(new Setting("DoubleTap", true, v -> page.getValue() == Page.TickBoost && tickBoost.getValue()));
    Setting<Integer> delay = register(new Setting("Delay", 180, 0, 500, v -> page.getValue() == Page.TickBoost && tickBoost.getValue() && doubleTap.getValue()));
    Setting<Double> cooldown = register(new Setting("Cooldown", 2.5d, 0.1d, 8.0d, v -> page.getValue() == Page.TickBoost && tickBoost.getValue()));
    Setting<Double> tickStepHeight = register(new Setting("TickStepHeight", 2.0d, 0.6d, 2.5d, v -> page.getValue() == Page.TickBoost && tickBoost.getValue()));
    Setting<Boolean> onlyOnHole = register(new Setting("OnlyOnHole", false, v -> page.getValue() == Page.TickBoost && tickBoost.getValue()));
    Setting<Boolean> onlyOnKbBoost = register(new Setting("OnlyOnKbBoost", false, v -> page.getValue() == Page.TickBoost && tickBoost.getValue()));
    Timer doubleTapTimer = new Timer();
    Timer cooldownTimer = new Timer();
    static Strafe INSTANCE;
    double distance;
    double lastDist;
    boolean boost;
    double speed;
    int stage;
    int runs;

    public static Strafe getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Strafe();
        }
        return INSTANCE;
    }


    @Override
    public void onEnable() {
        if (mc.player != null) {
            speed = MovementUtil.getSpeed();
            distance = MovementUtil.getDistanceXZ();
        }
        stage = 4;
        lastDist = 0;
        resetTickBoost();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) {
            return;
        }
        if (step.getValue()) {
            mc.player.stepHeight = 0.6f;
        }
        resetTickBoost();
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (!MovementUtil.isMoving() || isDisabled() || mc.player.isElytraFlying()) {
            return;
        }
        if (noLiquid.getValue() && EntityUtil.isInLiquid() || mc.player.isOnLadder() || (sneaking.getValue() == Sneaking.Pause && mc.player.isSneaking()) ||  Cascade.packetManager.getCaughtPPS()) {
            return;
        }

        if (step.getValue()) {
            MovementUtil.step(height.getValue());
        }
        if (kbStep.getValue() && !shouldBoost(true) && !step.getValue()) {
            mc.player.stepHeight = 0.6f;
        }

        if (PlayerUtil.isClipping()) {
            return;
        }
        doTickBoost();
        if (HoleUtil.isInHole(mc.player.getPosition())) {
            return;
        }
        if (stage == 1 && MovementUtil.isMoving()) {
            speed = 1.35 * MovementUtil.calcEffects(0.2873) - 0.01;
        } else if (stage == 2 && MovementUtil.isMoving()) {
            if (!EntityUtil.isInLiquid() && !mc.player.isInWeb) {
                double yMotion = 0.3999 + MovementUtil.getJumpSpeed();
                mc.player.motionY = yMotion;
                event.setY(yMotion);
            }
            if (shouldBoost(false) && kbStep.getValue()) {
                MovementUtil.step(kbHeight.getValue());
            }
            speed = shouldBoost(false) ? (kbFactor.getValue()) / 10.0d : speed * (boost ? 1.6835 : 1.395);
        } else if (stage == 3) {
            if (shouldBoost(true) && kbStep.getValue()) {
                MovementUtil.step(kbHeight.getValue());
            }
            speed = shouldBoost(false) ? (kbFactor.getValue()) / 10.0d : distance - 0.66 * (distance - MovementUtil.calcEffects(0.2873));
            boost = !boost;
        } else {
            if ((mc.world.getCollisionBoxes(null, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).size() > 0 || mc.player.collidedVertically) && stage > 0) {
                stage = MovementUtil.isMoving() ? 1 : 0;
            }
            speed = distance - distance / 159.0;
        }

        speed = Math.min(speed, MovementUtil.calcEffects(10.0));
        speed = Math.max(speed, MovementUtil.calcEffects(0.2873));
        MovementUtil.strafe(event, speed);
        if (MovementUtil.isMoving()) {
            stage++;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent e) {
        if (isDisabled()) {
            return;
        }
        if (!MovementUtil.isWasdPressed()) {
            MovementUtil.setMotion(0 , mc.player.motionY, 0);
        }

        distance = MovementUtil.getDistanceXZ();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive e) {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof SPacketPlayerPosLook) {
            distance = 0.0;
            speed = 0.0;
            stage = 4;
            if (tickBoost.getValue()) {
                resetTickBoost();
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send e) {
        if (isDisabled() || fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof CPacketEntityAction && sneaking.getValue() == Sneaking.Cancel) {
            CPacketEntityAction p = e.getPacket();
            if (p.getAction() == CPacketEntityAction.Action.START_SNEAKING) {
                mc.player.setSneaking(false);
                e.setCanceled(true);
            }
        }
    }


    boolean shouldBoost(boolean stepCheck) {
        return kbBoost.getValue() &&
                Cascade.packetManager.getCaughtE() &&
                Cascade.packetManager.getPacketE().getStrength() == 6.0 &&
                (stepCheck || mc.player.posY - Cascade.packetManager.getPacketE().posY >= -0.9) &&
                mc.player.getDistance(Cascade.packetManager.getPacketE().getX(), Cascade.packetManager.getPacketE().getY(), Cascade.packetManager.getPacketE().getZ()) <= 12.0;
    }

    void resetTickBoost() {
        doubleTapTimer.reset();
        cooldownTimer.reset();
        runs = 0;
    }

    void doTickBoost() {
        if (!tickBoost.getValue()) {
            return;
        }
        if (onlyOnHole.getValue() && !HoleUtil.isInHole(mc.player.getPosition()) || onlyOnKbBoost.getValue() && !shouldBoost(false)) {
            return;
        }
        if (!PlayerUtil.isInLiquid()) {
            MovementUtil.step(tickStepHeight.getValue().floatValue());
        }
        if (cooldownTimer.passedMs((int)(cooldown.getValue() * 1000))) {
            if (!doubleTap.getValue()) {
                if (runs < tickFactor.getValue()) {
                    runs++;
                    PhysicsUtil.runPhysicsTick();
                } else {
                    runs = 0;
                }
            } else {
                if (!doubleTapTimer.passedMs(5)) {
                    if (runs < tickFactor.getValue()) {
                        runs++;
                        PhysicsUtil.runPhysicsTick();
                    } else {
                        runs = 0;
                    }
                }
                if (doubleTapTimer.passedMs(delay.getValue())) {
                    if (runs < tickFactor.getValue()) {
                        runs++;
                        PhysicsUtil.runPhysicsTick();
                    } else {
                        runs = 0;
                    }
                }
            }
            cooldownTimer.reset();
        }
    }
}