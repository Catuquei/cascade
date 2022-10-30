package cascade.features.gui.components.items.buttons;

import cascade.Cascade;
import cascade.features.gui.components.Component;
import cascade.features.gui.components.items.Item;
import cascade.features.modules.Module;
import cascade.features.modules.core.ClickGui;
import cascade.features.setting.Bind;
import cascade.features.setting.ParentSetting;
import cascade.util.render.RenderUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Button {

    private final Module module;
    private List<Item> items = new ArrayList<>();
    private boolean subOpen;
    private float _y;
    float currSize;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        initSettings();
    }

    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<>();
        if (!module.getSettings().isEmpty()) {
            module.getSettings().forEach(setting -> {
                if (setting instanceof ParentSetting) {
                    newItems.add(new ParentButton((ParentSetting) setting));
                    return;
                }
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton(setting));
                }
                if (setting.getValue() instanceof Bind && !setting.getName().equalsIgnoreCase("Keybind") && !module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton(setting));
                }
                if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                    newItems.add(new StringButton(setting));
                }
                if (setting.getValue() instanceof Color)
                    newItems.add(new ColorButton(setting));

                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(new Slider(setting));
                    return;
                }
                if (!setting.isEnumSetting())
                    return;
                newItems.add(new EnumButton(setting));
            });
        }
        newItems.add(new BindButton(module.getSettingByName("Keybind")));
        items = newItems;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (isHovering(mouseX, mouseY) && ClickGui.getInstance().isEnabled() && ClickGui.getInstance().descriptions.getValue()) {
            RenderUtil.drawOutlinedRoundedRectangle(mouseX + 10, mouseY, mouseX + 10 + renderer.getStringWidth(module.getDescription()), mouseY + 10, 1.0f, 0, 0, 0, 100 , 1.0f);
            //RenderUtil.drawOutlineRect(mouseX + 10, mouseY, mouseX + 10 + renderer.getStringWidth(module.getDescription()), mouseY + 10, new Color(ColorUtil.toRGBA(ClickGui.getInstance().c.getValue().getRed(), ClickGui.getInstance().c.getValue().getGreen(), ClickGui.getInstance().c.getValue().getBlue(), 255)), 1f);
            renderer.drawStringWithShadow(module.getDescription(), mouseX + 10, mouseY, -1);
        }
        if (subOpen) {
            if (currSize > 0)
                currSize -= 0.1f;
        }
        if (!items.isEmpty()) {
            if (ClickGui.getInstance().moduleOutline.getValue()) {
                RenderUtil.drawOutlineRect(x, y + height, x + width, y, ClickGui.getInstance().moduleOutlineColor.getValue(), 0.1f);
            }
            if (subOpen) {
                float height = 1.0f;
                for (Item item : items) {
                    ++Component.counter1[0];
                    if (!item.isHidden()) {
                        item.setLocation(x + 1.0f, y + (height += 15.0f));
                        item.setHeight((int) 15.0f);
                        item.setWidth(width - 9);
                        item.drawScreen(mouseX, mouseY, partialTicks);
                        _y = height;
                        if (item instanceof ColorButton && ((ColorButton) item).setting.isOpen)
                            height += 110;
                        if (item instanceof EnumButton && ((EnumButton) item).setting.isOpen)
                            height += ((EnumButton) item).setting.getValue().getClass().getEnumConstants().length * 15;
                    }
                    item.update();
                }
                if (module.isEnabled()) {
                    RenderUtil.drawOutlineRect(x, y + 1, x + width, y + (_y + 16), getState() ? Cascade.colorManager.getColorWithAlphaColor(Cascade.moduleManager.getModuleByClass(ClickGui.class).c.getValue().getAlpha()) : ClickGui.getInstance().moduleOutlineColor.getValue(), 2f); //todo marked
                } else {
                    RenderUtil.drawOutlineRect(x, y, x + width, y + (_y + 16), ClickGui.getInstance().moduleOutlineColor.getValue(), 1f);
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!items.isEmpty()) {
            if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
                subOpen = !subOpen;
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            if (subOpen) {
                for (Item item : items) {
                    if (item.isHidden()) {
                        continue;
                    }
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!items.isEmpty() && subOpen) {
            for (Item item : items) {
                if (item.isHidden()) {
                    continue;
                }
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public int getHeight() {
        if (subOpen) {
            int height = 14;
            for (Item item : items) {
                if (item.isHidden()) {
                    continue;
                }
                if (item instanceof ColorButton && ((ColorButton) item).setting.isOpen) {
                    height += 110; //96
                } else {
                    height += item.getHeight() + 1;
                }
                if (item instanceof EnumButton && ((EnumButton) item).setting.isOpen) {
                    height += ((EnumButton) item).setting.getValue().getClass().getEnumConstants().length * 15; //+64
                }
            }
            return height + 2;
        }
        return 14;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void toggle() {
        module.toggle();
    }

    @Override
    public boolean getState() {
        return module.isEnabled();
    }
}