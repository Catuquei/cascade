package cascade.features.modules.core;

import cascade.features.modules.Module;
import cascade.features.setting.Setting;

public class TimingManager extends Module {

    public TimingManager() {
        super("TimingManager", Category.CORE, "");
        INSTANCE = this;
    }

    Setting<Double> explosionTime = register(new Setting("Explosion", 2.5d, 1.0d, 10.0d));
    Setting<Double> lagBackTime = register(new Setting("LagBack", 2.5d, 1.0d, 10.0d));
    Setting<Double> knockbackTime = register(new Setting("Knockback", 1.0d, 0.1d, 8.0d));
    Setting<Double> timerTime = register(new Setting("Timer", 3.2d, 0.1d, 8.0d));

    static TimingManager INSTANCE = new TimingManager();
    public static TimingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TimingManager();
        }
        return INSTANCE;
    }

    public double getExplosion() {
        return explosionTime.getValue() * 100.0d;
    }

    public double getLagBack() {
        return lagBackTime.getValue() * 100.0d;
    }

    public double getKnockback() {
        return knockbackTime.getValue() * 1000.0d;
    }

    public double getTimer() {
        return timerTime.getValue() * 1000.0d;
    }
}