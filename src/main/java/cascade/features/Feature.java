package cascade.features;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.modules.Module;
import cascade.features.setting.ParentSetting;
import cascade.features.setting.Setting;
import cascade.manager.TextManager;
import cascade.util.Util;

import java.util.ArrayList;
import java.util.List;

public class Feature implements Util {

    public List<Setting> settings = new ArrayList<>();
    public TextManager renderer = Cascade.textManager;
    private String name;

    public Feature() {
    }

    public Feature(String name) {
        this.name = name;
    }

    public static boolean nullCheck() {
        return mc.player == null;
    }

    public static boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    public String getName() {
        return this.name;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public boolean isEnabled() {
        if (this instanceof Module) {
            return ((Module) this).isOn();
        }
        return false;
    }

    public boolean isDisabled() {
        return !isEnabled();
    }

    public Setting register(Setting setting) {
        setting.setFeature(this);
        this.settings.add(setting);
        if (this instanceof Module && Feature.mc.currentScreen instanceof CascadeGui) {
            CascadeGui.getInstance().updateModule((Module) this);
        }
        return setting;
    }

    public ParentSetting registerParent(ParentSetting setting) {
        setting.setFeature(this);
        this.settings.add(setting);
        if (this instanceof Module && Feature.mc.currentScreen instanceof CascadeGui) {
            CascadeGui.getInstance().updateModule((Module) this);
        }
        return setting;
    }


    public Setting getSettingByName(String name) {
        for (Setting setting : this.settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public void reset() {
        for (Setting setting : this.settings) {
            setting.setValue(setting.getDefaultValue());
        }
    }

    public void clearSettings() {
        this.settings = new ArrayList<>();
    }
}

