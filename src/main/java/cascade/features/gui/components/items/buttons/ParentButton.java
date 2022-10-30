package cascade.features.gui.components.items.buttons;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.modules.core.ClickGui;
import cascade.features.setting.ParentSetting;
import cascade.features.setting.Setting;
import cascade.util.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ParentButton extends Button {
    public ParentSetting parentSetting;
    float currSize;

    public ParentButton(ParentSetting parentSetting) {
        super(parentSetting.getName());
        this.parentSetting = parentSetting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x + 1, y, x + width + 6, y + height, isHovering(mouseX, mouseY) ? Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).c.getValue().getAlpha()) : Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue()));
        RenderUtil.drawOutlineRect(x + 1, y, x + width + 6, y + height, new Color(1), 1f);
        mc.fontRenderer.drawStringWithShadow(parentSetting.getName(), x + 3, y + (height / 2f) - (mc.fontRenderer.FONT_HEIGHT / 2f) + 1, -1);
        if (parentSetting.isOpened()) {
            if (currSize > 0)
                currSize -= 0.1f;
            int i = 0;
            for (Setting setting : parentSetting.getChildren()) {
                if (setting.isVisible())
                    i += 15;
                if (setting.getValue() instanceof Color && setting.isOpen)
                    i += 110;
                if (setting.getValue() instanceof Enum && setting.isOpen)
                    i += setting.getValue().getClass().getEnumConstants().length * 15;
            }
            RenderUtil.drawOutlineRect(x + 1, y + 1, x + width + 6, y + height + i, new Color(Cascade.colorManager.getColorWithAlpha(Cascade.moduleManager.getModuleByClass(ClickGui.class).hoverAlpha.getValue())), 2f);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (isHovering(mouseX, mouseY) && mouseButton == 1) {
            parentSetting.setOpened(!parentSetting.getValue());
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }
}
