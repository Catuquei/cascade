package cascade.features.gui.components.items.buttons;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.gui.components.Component;
import cascade.features.gui.components.items.Item;
import cascade.features.modules.core.ClickGui;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import sun.security.pkcs11.Secmod;

import java.awt.*;

public class Button extends Item {
    boolean state;

    public Button(String name) {
        super(name);
        height = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //old, works but has that issue with right pixels missing
        //RenderUtil.drawRect(x + currWidth, y, x + (float) width, y + (float) height, (isHovering(mouseX, mouseY) ? -2007673515 : 0x11555555));
        //RenderUtil.drawRect(x, y, x + currWidth, y + (float) height, isHovering(mouseX, mouseY) ? Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).c.getValue().getAlpha()) : Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).hoverA.getValue()));

        //new, fixed the missing pixels but it renders all modules with enabledColor
        //RenderUtil.drawRect(x , y, x + (float) width, y + (float) height, (isHovering(mouseX, mouseY) ? -2007673515 : 0x11555555));
        //RenderUtil.drawRect(x, y, x + width, y + (float) height, isHovering(mouseX, mouseY) ? Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).c.getValue().getAlpha()) : Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).hoverA.getValue()));

        //ccshack
        Color guiEnabledBox = new Color(ClickGui.getInstance().moduleEnabledBox.getValue().getRed(),ClickGui.getInstance().moduleEnabledBox.getValue().getGreen(), ClickGui.getInstance().moduleEnabledBox.getValue().getBlue(), ClickGui.getInstance().moduleEnabledBox.getValue().getAlpha());
        Color guiDisabledBox = new Color(ClickGui.getInstance().moduleDisabledBox.getValue().getRed(),ClickGui.getInstance().moduleDisabledBox.getValue().getGreen(), ClickGui.getInstance().moduleDisabledBox.getValue().getBlue(), ClickGui.getInstance().moduleDisabledBox.getValue().getAlpha());
        Color guiEnabledText = new Color(ClickGui.getInstance().moduleEnabledText.getValue().getRed(),ClickGui.getInstance().moduleEnabledText.getValue().getGreen(), ClickGui.getInstance().moduleEnabledText.getValue().getBlue(), ClickGui.getInstance().moduleEnabledText.getValue().getAlpha());
        Color guiDisabledText = new Color(ClickGui.getInstance().moduleDisabledText.getValue().getRed(),ClickGui.getInstance().moduleDisabledText.getValue().getGreen(), ClickGui.getInstance().moduleDisabledText.getValue().getBlue(), ClickGui.getInstance().moduleDisabledText.getValue().getAlpha());
        Color textColor = new Color(-5592406);
        Color boxColor = new Color(-1);
        //todo isHovering NIGGGA!!!!!
        if (getState()) {
            textColor = guiEnabledText;
            boxColor = guiEnabledBox;
        } else {
            textColor = guiDisabledText;
            boxColor = guiDisabledBox;
        }
        RenderUtil.drawRect(x , y, x + width, y + height, ColorUtil.toRGBA(boxColor));
        RenderUtil.drawGradient(x , y, x + width, y + height, ColorUtil.toRGBA(0, 0, 0, 0), getState() ? ColorUtil.toRGBA(ClickGui.getInstance().gradientEnabled.getValue().getRed(), ClickGui.getInstance().gradientEnabled.getValue().getGreen(), ClickGui.getInstance().gradientEnabled.getValue().getBlue(), ClickGui.getInstance().gradientEnabled.getValue().getAlpha()) : ColorUtil.toRGBA(ClickGui.getInstance().gradientDisabled.getValue().getRed(), ClickGui.getInstance().gradientDisabled.getValue().getGreen(), ClickGui.getInstance().gradientDisabled.getValue().getBlue(), ClickGui.getInstance().gradientDisabled.getValue().getAlpha()));
        Cascade.textManager.drawStringWithShadow(getName(), x + (isHovering(mouseX, mouseY) ? 3.3f : 2.3f), y - 2.0f - (float) CascadeGui.getClickGui().getTextOffset(), ColorUtil.toRGBA(textColor));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            onMouseClick();
        }
    }

    public void onMouseClick() {
        state = !state;
        toggle();
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return state;
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : CascadeGui.getClickGui().getComponents()) {
            if (!component.drag) {
                continue;
            }
            return false;
        }
        return (float) mouseX >= getX() && (float) mouseX <= getX() + (float) getWidth() && (float) mouseY >= getY() && (float) mouseY <= getY() + (float) height;
    }
}