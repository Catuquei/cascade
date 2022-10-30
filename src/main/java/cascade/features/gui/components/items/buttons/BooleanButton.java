package cascade.features.gui.components.items.buttons;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.setting.Setting;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import cascade.features.modules.core.ClickGui;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;

import static net.minecraftforge.fml.client.config.GuiUtils.drawGradientRect;

public class BooleanButton extends Button {

    Setting setting;

    public BooleanButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, !isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515);
        /*if (getState()) {
            //x + width - 7, y + 2, x + width + 4, y + height - 2 //-+-+ to decrease
            //RenderUtil.drawRect(x + width - 6, y + 3, x + width + 3, y + height - 3, (!isHovering(mouseX, mouseY) ? Cascade.colorManager.getColorWithAlpha(ClickGui.getInstance().hoverA.getValue()) : ColorUtil.toRGBA(ClickGui.getInstance().c.getValue())));
            //x + width - 5, y + 4.5f, x + width + 2, y + height - 4 >> shit i used for drawRect(booleanOutline)
            RenderUtil.drawRect(x + width - 6, y + 3, x + width + 3, y + height - 3, ColorUtil.toRGBA(ClickGui.getInstance().booleanOutline.getValue()));
        } else {
            RenderUtil.drawRect(x + width - 6, y + 3, x + width + 3, y + height - 3, ColorUtil.toRGBA(ClickGui.getInstance().booleanDisabled.getValue()));
        }*/
        RenderUtil.drawRect(x + width - 6, y + 3, x + width + 3, y + height - 3, getState() ? ColorUtil.toRGBA(ClickGui.getInstance().booleanOutline.getValue()) : ColorUtil.toRGBA(ClickGui.getInstance().booleanDisabled.getValue()));
        RenderUtil.drawOutlineRect(x + width - 6, y + 3, x + width + 3, y + height - 3, ClickGui.getInstance().booleanDisabledOut.getValue(), 0.1f);
        RenderUtil.drawGradient(x + width - 5.9f, y + 3.1f, x + width + 3.1f, y + height - 2.5f, ColorUtil.toRGBA(0, 0, 0, 0), ColorUtil.toRGBA(40, 40, 40, 99));
        Cascade.textManager.drawStringWithShadow(getName(), x + 2.3f, y - 1.7f - (float) CascadeGui.getClickGui().getTextOffset(), getState() ? -1 : -5592406);
    }

    /*
    skeet ->
        25, 26, 26, 255 //disabled boolean fill
        45, 45, 45, 255 //
        51, 51, 51, 255 //
        40, 40, 40, 255 //boolean outline
        51, 51, 51, 255 //
        45, 45, 45, 255 //
        16, 16, 16, 255 //background gui color
    cascade ->
        25, 25, 25, 255
        50, 50, 50, 255
     */

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
        setting.setValue((Boolean)setting.getValue() == false);
    }

    @Override
    public boolean getState() {
        return (Boolean) setting.getValue();
    }
}