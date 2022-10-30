package cascade.features.modules.movement;

import cascade.util.entity.EntityUtil;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.player.MovementUtil;

public class Step extends Module {

    public Step() {
        super("Step", Category.MOVEMENT, "Allows you to step up blocks");
    }

    Setting<Float> height = register(new Setting("Height", 2.0f, 0.1f, 2.5f));
    Setting<Boolean> noLiquid = register(new Setting("NoLiquid", true));

    @Override
    public void onToggle() {
        if (mc.player != null) {
            mc.player.stepHeight = 0.6f;
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (noLiquid.getValue() && EntityUtil.isInLiquid()) {
            return;
        }
        MovementUtil.step(height.getValue());
    }
}