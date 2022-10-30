package cascade.features.gui.components.items.buttons;

import cascade.Cascade;
import cascade.features.gui.CascadeGui;
import cascade.features.modules.core.ClickGui;
import cascade.features.setting.Setting;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class EnumButton extends Button {

    public Setting setting;

    public EnumButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(x, y, x + (float) width + 7.4f, y + (float) height - 0.5f, (!isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        Cascade.textManager.drawStringWithShadow(setting.getName() + " " + ChatFormatting.GRAY + (setting.currentEnumName().equalsIgnoreCase("ABC") ? "ABC" : setting.currentEnumName()), x + 2.3f, y - 1.7f - (float) CascadeGui.getClickGui().getTextOffset(), getState() ? -1 : -5592406);
        int y = (int) this.y;
        if (setting.isOpen) {
            for (Object obber : setting.getValue().getClass().getEnumConstants()) {
                y += height;
                if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height)
                    RenderUtil.drawRect(x + 2, y, x + width + 7f, y + height, new Color(0x64000001, true).getRGB());
                Cascade.textManager.drawStringWithShadow((setting.getValueAsString().equals(obber.toString()) ? ChatFormatting.WHITE : ChatFormatting.GRAY) + obber.toString(), x + 4, y + (height / 2f) - (mc.fontRenderer.FONT_HEIGHT / 2f), -1);
            }
            RenderUtil.drawOutlineRect(x + 3, this.y + height - 1, x + width + 5f, y + height - 2, new Color(ColorUtil.toRGBA(ClickGui.getInstance().enumOutline.getValue())), 1f);
        }
    }

    @Override
    public void update() {
        setHidden(!setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int y = (int) this.y;
        if (setting.isOpen) {
            for (Object obber : setting.getValue().getClass().getEnumConstants()) {
                y += height;
                if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height && mouseButton == 0) {
                    setting.setEnumValue(String.valueOf(obber));
                }
            }
        }
        if (isHovering(mouseX, mouseY) && mouseButton == 1) {
            setting.isOpen = !setting.isOpen;
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public boolean getState() {
        return true;
    }
}