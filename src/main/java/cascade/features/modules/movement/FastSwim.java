package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.MoveEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.modules.exploit.FastMotion;
import cascade.features.modules.player.Freecam;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.player.MovementUtil;
import cascade.util.player.PlayerUtil;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraft.network.play.client.CPacketEntityAction.Action.START_SPRINTING;

public class FastSwim extends Module {

    public FastSwim() {
        super("FastSwim", Category.MOVEMENT, "Makes u go faster in liquids");
    }

    Setting<Boolean> useTimer = register(new Setting("UseTimer", true));
    Setting<Boolean> noLag = register(new Setting("NoLag", true));
    Setting<Boolean> flight = register(new Setting("Flight", false));
    //Setting<Boolean> limitMotion = register(new Setting("LimitMotion", true));

    Setting<Double> wHorizontal = register(new Setting("WaterHorizontal", 2.0, 0.0, 20.0));
    Setting<Double> wUp = register(new Setting("WaterUp", 2.0, 0.0, 20.0));
    Setting<Double> wDown = register(new Setting("WaterDown", 2.0, 0.0, 20.0));

    Setting<Double> lHorizontal = register(new Setting("LavaHorizontal", 1.0, 0.0, 20.0));
    Setting<Double> lUp = register(new Setting("LavaUp", 1.0, 0.0, 20.0));
    Setting<Double> lDown = register(new Setting("LavaDown", 1.0, 0.0, 20.0));

    Setting<Boolean> kbBoost = register(new Setting("KbBoost", true));
    Setting<Boolean> onGroundBypass = register(new Setting("OnGroundBypass", true, v -> kbBoost.getValue()));
    Setting<Float> factor = register(new Setting("Factor", 16.0f, 0.1f, 20.0f, v -> kbBoost.getValue()));

    @Override
    public void onDisable() {
        if (useTimer.getValue() && ((ITimer) mc.timer).getTickLength() != 50f) {
            Cascade.timerManager.reset();
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent e) {
        if (isDisabled()) {
            return;
        }
        if (Freecam.getInstance().isEnabled()) {
            return;
        }
        if (noLag.getValue() && Cascade.packetManager.getCaughtPPS()) {
            return;
        }
        if (FastMotion.getInstance().isEnabled() && FastMotion.getInstance().shouldBoost()) {
            return;
        }
        if (PlayerUtil.isInLiquid()) {
            if (useTimer.getValue()) {
                Cascade.timerManager.set(1.0888f);
            }
            mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, START_SPRINTING));
            mc.player.setSprinting(true);
            if (mc.player.isInLava()) {
                if (MovementUtil.isMoving()) {
                    if (shouldBoost()) {
                        if (onGroundBypass.getValue()) {
                            mc.player.onGround = false;
                        }
                        e.setX(e.getX() * factor.getValue());
                        e.setZ(e.getZ() * factor.getValue());
                    } else {
                        e.setX(e.getX() * lHorizontal.getValue());
                        e.setZ(e.getZ() * lHorizontal.getValue());
                    }
                }
                if ((flight.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) ||
                    (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown())) {
                    e.setY(0.0d);
                    mc.player.motionY = 0.0d;
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    e.setY(e.getY() * lDown.getValue());
                }
                if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                    e.setY(e.getY() * lUp.getValue());
                }
            } else {
                if (MovementUtil.isMoving()) {
                    e.setX(e.getX() * wHorizontal.getValue() / 2.0d);
                    e.setZ(e.getZ() * wHorizontal.getValue() / 2.0d);
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown() && mc.gameSettings.keyBindJump.isKeyDown()) {
                    e.setY(-0.05);
                }
                if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                    e.setY(e.getY() * wUp.getValue()); //0.098
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    e.setY(e.getY() * wDown.getValue());
                }
            }
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent e) {
        if (fullNullCheck() || isDisabled() || Freecam.getInstance().isEnabled() ||
           (noLag.getValue() && Cascade.packetManager.getCaughtPPS()) ||
           (FastMotion.getInstance().isEnabled() && FastMotion.getInstance().shouldBoost())) {
            return;
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown() && PlayerUtil.isInLiquid() && MovementUtil.isMoving()) {
            e.getMovementInput().moveStrafe *= 5.0f;
            e.getMovementInput().moveForward *= 5.0f;
        }
    }

    boolean shouldBoost() {
        return kbBoost.getValue() && (!mc.player.onGround || onGroundBypass.getValue()) && Cascade.packetManager.isValid();
    }

    /*boolean shouldBoost() {
        return kbBoost.getValue() &&
               Cascade.packetManager.getCaughtE() &&
               Cascade.packetManager.getPacketE().getStrength() == 6.0 &&
               (!mc.player.onGround || onGroundBypass.getValue()) &&
               mc.player.posY - Cascade.packetManager.getPacketE().posY >= -0.9 &&
               mc.player.getDistance(Cascade.packetManager.getPacketE().getX(), Cascade.packetManager.getPacketE().getY(), Cascade.packetManager.getPacketE().getZ()) <= 12.0;
    }*/

    //velocity = Math.abs(((SPacketExplosion) event.getPacket()).motionX) + Math.abs(((SPacketExplosion) event.getPacket()).motionZ);
}