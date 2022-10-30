package cascade.features.modules.movement;

import cascade.Cascade;
import cascade.event.events.MoveEvent;
import cascade.event.events.PacketEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.modules.player.Freecam;
import cascade.features.setting.Setting;
import cascade.mixin.mixins.accessor.ITimer;
import cascade.util.player.MovementUtil;
import cascade.util.player.PlayerUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", Module.Category.MOVEMENT, "omg no slow");
        INSTANCE = this;
    }

    Setting<Boolean> invMove = register(new Setting("InvMove", true));
    Setting<Boolean> ice = register(new Setting("Ice", true));
    public Setting<Boolean> soulSand = register(new Setting("SoulSand", true));
    Setting<Boolean> strict = register(new Setting("Strict", false));

    Setting<Boolean> webs = register(new Setting("Webs", true));
    Setting<WebMode> webMode = register(new Setting("WebMode", WebMode.Motion, v -> webs.getValue()));
    enum WebMode {Motion, Timer}
    Setting<Float> factor = register(new Setting("Factor", 1.0f, 0.1f, 20.0f, v -> webs.getValue()));
    Setting<Boolean> strafe = register(new Setting("Strafe", true, v -> webs.getValue()));
    private static NoSlow INSTANCE;

    static KeyBinding[] keys = new KeyBinding[] {
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSprint
    };

    public static NoSlow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoSlow();
        }
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        if (fullNullCheck()) {
            return;
        }
        if (webs.getValue() && webMode.getValue() == WebMode.Timer && ((ITimer)mc.timer).getTickLength() != 50) {
            Cascade.timerManager.reset();
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (ice.getValue()) {
            Blocks.ICE.setDefaultSlipperiness(0.6f);
            Blocks.PACKED_ICE.setDefaultSlipperiness(0.6f);
            Blocks.FROSTED_ICE.setDefaultSlipperiness(0.6f);
        }
        if (invMove.getValue()) {
            if (mc.currentScreen instanceof GuiChat || mc.currentScreen == null) {
                return;
            }
            for (KeyBinding bind : keys) {
                if (Keyboard.isKeyDown(bind.getKeyCode())) {
                    if (bind.getKeyConflictContext() != KeyConflictContext.UNIVERSAL) {
                        bind.setKeyConflictContext(KeyConflictContext.UNIVERSAL);
                    }
                    KeyBinding.setKeyBindState(bind.getKeyCode(), true);
                } else {
                    KeyBinding.setKeyBindState(bind.getKeyCode(), false);
                }
            }
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent e) {
        if (isDisabled()) {
            return;
        }
        if (webs.getValue() && mc.player.isInWeb) {
            if (Freecam.getInstance().isEnabled()) {
                return;
            }
            if (strafe.getValue()) {
                MovementUtil.strafe(e, MovementUtil.getSpeed());
            }
            if (webMode.getValue() == WebMode.Motion) {
                if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    e.setY(-factor.getValue());
                }
            } else {
                if (!mc.player.onGround && mc.gameSettings.keyBindSneak.isKeyDown()) {
                    Cascade.timerManager.set(factor.getValue());
                }
            }
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent e) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (mc.player.isHandActive() && !mc.player.isRiding()) {
            if (strict.getValue()) {
                mc.getConnection().sendPacket(new CPacketHeldItemChange(mc.player.inventory.currentItem));
            }
            e.getMovementInput().moveStrafe *= 5.0f;
            e.getMovementInput().moveForward *= 5.0f;
        }
    }
}