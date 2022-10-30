package cascade.features.setting;

import java.util.ArrayList;
import java.util.function.Predicate;

public class ParentSetting extends Setting<Boolean> {
    ArrayList<Setting> children = new ArrayList<>();

    public ParentSetting(String name) {
        super(name, false);
    }

    public ParentSetting(String name, Predicate<Boolean> visibility) {
        super(name, false, visibility);
    }

    public boolean isOpened() {
        return getValue();
    }

    public ArrayList<Setting> getChildren() {
        return children;
    }

    public void addChild(Setting setting){
        children.add(setting);
    }

    public void setOpened(boolean value) {
        this.value = value;
    }
}
