package cascade.features.gui.components.items.buttons;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.gui.components.Component;
import cascade.features.setting.Setting;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import cascade.features.modules.core.ClickGui;
import org.lwjgl.input.Mouse;

public class Slider
        extends Button {
    private final Number min;
    private final Number max;
    private final int difference;
    public Setting setting;

    public Slider(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        min = (Number) setting.getMin();
        max = (Number) setting.getMax();
        difference = max.intValue() - min.intValue();
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        dragSetting(mouseX, mouseY);
        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, 0x11555555);
        RenderUtil.drawRectCol(x, y + 13.3f, ((Number) setting.getValue()).floatValue() <= min.floatValue() ? 0 : ((float) width + 7.4f) * partialMultiplier(), height - 14f, !isHovering(mouseX, mouseY) ? Cascade.colorManager.getColorWithAlphaColor(Cascade.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue()) : Cascade.colorManager.getColorWithAlphaColor(Cascade.moduleManager.getModuleByClass(ClickGui.class).c.getValue().getAlpha()));
        Cascade.textManager.drawStringWithShadow(getName() + " " + ChatFormatting.GRAY + (setting.getValue() instanceof Float ? setting.getValue() : (setting.getValue() instanceof Integer ? Integer.valueOf(((Integer) setting.getValue()).intValue()) : Double.valueOf(((Number)setting.getValue()).doubleValue()))), x + 2.3f, y - 1.3f - (float) CascadeGui.getClickGui().getTextOffset(), -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY) && mc.currentScreen instanceof CascadeGui) {
            setSettingFromX(mouseX);
        }
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : CascadeGui.getClickGui().getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float) mouseX >= getX() && (float) mouseX <= getX() + (float) getWidth() + 8.0f && (float) mouseY >= getY() && (float) mouseY <= getY() + (float) height;
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    private void dragSetting(int mouseX, int mouseY) {
        if (isHovering(mouseX, mouseY) && Mouse.isButtonDown(0) && mc.currentScreen instanceof CascadeGui) {
            setSettingFromX(mouseX);
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    private void setSettingFromX(int mouseX) {
        float percent = ((float) mouseX - x) / ((float) width + 7.4f);
        if (setting.getValue() instanceof Double) {
            double result = (Double) setting.getMin() + (double) ((float) difference * percent);
            setting.setValue((double) Math.round(10.0 * result) / 10.0);
        } else if (setting.getValue() instanceof Float) {
            float result = ((Float) setting.getMin()).floatValue() + (float) difference * percent;
            setting.setValue(Float.valueOf((float) Math.round(10.0f * result) / 10.0f));
        } else if (setting.getValue() instanceof Integer) {
            setting.setValue((Integer) setting.getMin() + (int) ((float) difference * percent));
        }
    }

    private float middle() {
        return max.floatValue() - min.floatValue();
    }

    private float part() {
        return ((Number) setting.getValue()).floatValue() - min.floatValue();
    }

    private float partialMultiplier() {
        return part() / middle();
    }
}

