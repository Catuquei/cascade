package cascade.features.modules;

import cascade.Cascade;
import cascade.event.events.ClientEvent;
import cascade.event.events.ModuleToggleEvent;
import cascade.event.events.Render2DEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.Feature;
import cascade.features.modules.core.ClientManagement;
import cascade.features.modules.misc.ChatBridge;
import cascade.features.setting.Bind;
import cascade.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//todo burrow, autoarmor, killaura, buildheight, extratab, nosoundlag, packets, portals, tooltips, esp, itemchams, logoutspots, norender, trajectories
public class Module extends Feature {
    String description;
    Category category;
    public Setting<Boolean> message = register(new Setting<>("Message", true));
    public Setting<Boolean> enabled = register(new Setting<>("Enabled", false));
    public Setting<Boolean> drawn = register(new Setting<>("Drawn", true));
    public Setting<Bind> bind = register(new Setting<>("Keybind", new Bind(-1)));
    public String name;
    Module module;

    public Module(String name, Category category, String description) {
        super(name);
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onToggle() {
    }

    public void onLoad() {
    }

    public void onTick() {
    }

    public void onLogin() {
    }

    public void onLogout() {
    }

    public void onUpdate() {
    }

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public String getDisplayInfo() {
        return null;
    }

    public void setEnabled(boolean enabled) {
        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void enable() {
        enabled.setValue(true);
        onToggle();
        onEnable();
        if (module.shouldNotify()) {
            TextComponentString text = new TextComponentString((ClientManagement.getInstance().toggleName.getValue() ? (Cascade.chatManager.getClientMessage() + " ") : "") + ChatFormatting.RESET + ChatFormatting.BOLD + module.getName() + ChatFormatting.GREEN + " enabled.");
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
        MinecraftForge.EVENT_BUS.post(new ModuleToggleEvent.Enable(this));
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void disable() {
        enabled.setValue(false);
        onToggle();
        onDisable();
        if (module.shouldNotify()) {
            TextComponentString text = new TextComponentString((ClientManagement.getInstance().toggleName.getValue() ? (Cascade.chatManager.getClientMessage() + " ") : "")  + ChatFormatting.RESET + ChatFormatting.BOLD + module.getName() + ChatFormatting.RED + " disabled.");
            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(text, 1);
        }
        MinecraftForge.EVENT_BUS.post(new ModuleToggleEvent.Disable(this));
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void toggle() {
        ClientEvent event = new ClientEvent(!this.isEnabled() ? 1 : 0, this);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            this.setEnabled(!this.isEnabled());
        }
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean shouldNotify() {
        return this.message.getValue();
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public Category getCategory() {
        return this.category;
    }

    public String getInfo() {
        return null;
    }
    
    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }

    public boolean isOn() {
        return this.enabled.getValue();
    }

    public String getFullArrayString() {
        return this.getName() + ChatFormatting.GRAY + (this.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + this.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
    }

    public enum Category {
        VISUAL("Visual"),
        COMBAT("Combat"),
        EXPLOIT("Exploit"),
        MISC("Misc"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        CORE("Core"),
        HUD("HUD");

        String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    @SubscribeEvent
    public void onModuleToggle(ClientEvent e) {
        if (e.getStage() == 0 || e.getStage() == 1) {
            module = (Module)e.getFeature();
        }
    }
}