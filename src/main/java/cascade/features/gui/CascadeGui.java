package cascade.features.gui;

import cascade.Cascade;
import cascade.features.Feature;
import cascade.features.gui.components.Component;
import cascade.features.gui.components.items.Item;
import cascade.features.gui.components.items.buttons.ModuleButton;
import cascade.features.modules.Module;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class CascadeGui extends GuiScreen {

    private static CascadeGui INSTANCE;

    static {
        INSTANCE = new CascadeGui();
    }

    private final ArrayList<Component> components = new ArrayList();

    public CascadeGui() {
        this.setInstance();
        this.load();
    }

    public static CascadeGui getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CascadeGui();
        }
        return INSTANCE;
    }

    public static CascadeGui getClickGui() {
        return CascadeGui.getInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    /*@Override
    public void initGui() {
        if (OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer && ClickGui.getInstance().blur.getValue() != ClickGui.Blur.none) {
            if (this.mc.entityRenderer.getShaderGroup() != null)
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/" + ClickGui.getInstance().blur.getValue() + ".json"));
        }
    }*/

    @Override
    public void onGuiClosed() {
        if (mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    void load() {
        int x = -106;
        for (Module.Category category : Cascade.moduleManager.getCategories()) {
            components.add(new Component(category.getName(), x += 110, 4, true) {

                @Override
                public void setupItems() {
                    counter1 = new int[]{1};
                    Cascade.moduleManager.getModulesByCategory(category).forEach(module -> {
                        addButton(new ModuleButton(module));
                    });
                }
            });
        }
        components.forEach(components -> components.getItems().sort(Comparator.comparing(Feature::getName)));
    }

    public void updateModule(Module module) {
        for (Component component : components) {
            for (Item item : component.getItems()) {
                if (!(item instanceof ModuleButton)) {
                    continue;
                }
                ModuleButton button = (ModuleButton) item;
                Module mod = button.getModule();
                if (module == null || !module.equals(mod)) {
                    continue;
                }
                button.initSettings();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        checkMouseWheel();
        components.forEach(components -> components.drawScreen(mouseX, mouseY, partialTicks));
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        components.forEach(components -> components.mouseClicked(mouseX, mouseY, clickedButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        components.forEach(components -> components.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return components;
    }

    public void checkMouseWheel() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            components.forEach(component -> component.setY(component.getY() - 10));
        } else if (dWheel > 0) {
            components.forEach(component -> component.setY(component.getY() + 10));
        }
    }

    public int getTextOffset() {
        return -6;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }
}