package cascade.manager;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.Feature;
import cascade.features.gui.CascadeGui;
import cascade.features.modules.Module;
import cascade.features.modules.combat.*;
import cascade.features.modules.core.*;
import cascade.features.modules.core.TimingManager;
import cascade.features.modules.exploit.*;
import cascade.features.modules.hud.*;
import cascade.features.modules.misc.*;
import cascade.features.modules.movement.*;
import cascade.features.modules.player.*;
import cascade.features.modules.visual.*;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager extends Feature {

    public ArrayList<Module> mods = new ArrayList();
    public List<Module> sortedModules = new ArrayList<>();
    public List<String> sortedModulesABC = new ArrayList<>();

    public void init() {
        /** CORE */
        mods.add(new ClickGui());
        mods.add(new ClientManagement());
        mods.add(new FontMod());
        mods.add(new HUD());
        mods.add(new TimingManager());

        /** COMBAT */
        mods.add(new Aura());
        mods.add(new AutoArmor());
        mods.add(new Burrow());
        mods.add(new Crits());
        mods.add(new CascadeAura());
        mods.add(new HoleFiller());
        mods.add(new HoleFillR());
        mods.add(new Offhand());
        mods.add(new Surround());
        mods.add(new TrapR());
        mods.add(new WebAura());

        /** EXPLOIT */
        mods.add(new ChorusDelay());
        mods.add(new Clip());
        mods.add(new CoordExploit());
        mods.add(new FastMotion());
        mods.add(new HubTP());
        mods.add(new PacketFly());

        /** MISC */
        mods.add(new AutoReply());
        mods.add(new ChatBridge());
        mods.add(new ChorusPredict());
        mods.add(new EntityTrace());
        mods.add(new FakePlayer());
        mods.add(new IgnoreUnicode());
        mods.add(new KillEffect());
        mods.add(new LogCoords());
        mods.add(new LogSpots());
        mods.add(new NoForceRotate());
        mods.add(new NoInteract());
        mods.add(new NoSuffocation());
        mods.add(new Notifications());
        //mods.add(new ToolTips());
        mods.add(new TrueDurability());

        /** VISUAL */
        mods.add(new Ambience());
        mods.add(new BlockHighlight());
        mods.add(new CameraClip());
        mods.add(new Chams());
        mods.add(new CrystalChams());
        mods.add(new EntityTrails());
        mods.add(new ESP());
        mods.add(new GlintMod());
        mods.add(new HandChams());
        mods.add(new HitMarkers());
        mods.add(new HoleESP());
        //mods.add(new Nametags());
        mods.add(new NoRender());
        mods.add(new OffscreenESP());
        mods.add(new ViewMod());
        mods.add(new Visual());
        mods.add(new PopChams());
        mods.add(new Crosshair());

        /** MOVEMENT */
        mods.add(new AirStrafe());
        mods.add(new AntiVoid());
        mods.add(new AutoCenter());
        mods.add(new FastFall());
        mods.add(new FastSwim());
        mods.add(new Flight());
        mods.add(new HoleSnap());
        mods.add(new LongJump());
        mods.add(new NoSlow());
        mods.add(new Sprint());
        mods.add(new Step());
        mods.add(new Strafe());
        mods.add(new TickBoost());
        mods.add(new Velocity());
        mods.add(new YPort());

        /** PLAYER */
        mods.add(new AntiAim());
        mods.add(new PacketUse());
        mods.add(new FastUse());
        mods.add(new Freecam());
        mods.add(new LiquidInteract());
        mods.add(new Mine());
        mods.add(new Nuker());
        mods.add(new Refill());
        mods.add(new Scaffold());
        mods.add(new XCarry());

        /** HUD */
        mods.add(new Coords());
        mods.add(new HUDManager());
        mods.add(new Ping());
        mods.add(new PotionInfo());
        mods.add(new PvpInfo());
        mods.add(new Watermark());
    }


    public Module getModuleByName(String name) {
        for (Module module : this.mods) {
            if (!module.getName().equalsIgnoreCase(name)) continue;
            return module;
        }
        return null;
    }

    public <T extends Module> T getModuleByClass(Class<T> clazz) {
        for (Module module : this.mods) {
            if (!clazz.isInstance(module)) continue;
            return (T) module;
        }
        return null;
    }

    public boolean isModuleEnabled(String name) {
        Module module = this.getModuleByName(name);
        return module != null && module.isEnabled();
    }

    public ArrayList<Module> getEnabledModules() {
        ArrayList<Module> enabledModules = new ArrayList<>();
        for (Module module : this.mods) {
            if (!module.isEnabled()) continue;
            enabledModules.add(module);
        }
        return enabledModules;
    }

    public ArrayList<String> getEnabledModulesName() {
        ArrayList<String> enabledModules = new ArrayList<>();
        for (Module module : this.mods) {
            if (!module.isEnabled() || !module.isDrawn()) continue;
            enabledModules.add(module.getFullArrayString());
        }
        return enabledModules;
    }

    public ArrayList<Module> getModulesByCategory(Module.Category category) {
        ArrayList<Module> modulesCategory = new ArrayList<>();
        this.mods.forEach(module -> {
            if (module.getCategory() == category) {
                modulesCategory.add(module);
            }
        });
        return modulesCategory;
    }

    public List<Module.Category> getCategories() {
        return Arrays.asList(Module.Category.values());
    }

    public void onLoad() {
        mods.forEach(MinecraftForge.EVENT_BUS::register);
        mods.forEach(Module::onLoad);
    }

    public void onUpdate() {
        try {
            if (!fullNullCheck()) {
                this.mods.stream().filter(Feature::isEnabled).forEach(Module::onUpdate);
            }
        } catch (Exception ex) {
            Cascade.LOGGER.info("Caught an exception from ModuleManager");
            ex.printStackTrace();
        }
    }

    public void onTick() {
        this.mods.stream().filter(Feature::isEnabled).forEach(Module::onTick);
    }

    public void onRender2D(Render2DEvent event) {
        this.mods.stream().filter(Feature::isEnabled).forEach(module -> module.onRender2D(event));
    }

    public void onRender3D(Render3DEvent event) {
        this.mods.stream().filter(Feature::isEnabled).forEach(module -> module.onRender3D(event));
    }

    public void sortModules(boolean reverse) {
        this.sortedModules = this.getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing(module -> renderer.getStringWidth(module.getFullArrayString()) * (reverse ? -1 : 1))).collect(Collectors.toList());
    }

    public void sortModulesABC() {
        this.sortedModulesABC = new ArrayList<>(this.getEnabledModulesName());
        this.sortedModulesABC.sort(String.CASE_INSENSITIVE_ORDER);
    }

    public void onLogout() {
        this.mods.forEach(Module::onLogout);
    }

    public void onLogin() {
        this.mods.forEach(Module::onLogin);
    }

    public void onUnload() {
        this.mods.forEach(MinecraftForge.EVENT_BUS::unregister);
    }

    public void onUnloadPost() {
        for (Module module : this.mods) {
            module.enabled.setValue(false);
        }
    }

    public void onKeyPressed(int eventKey) {
        if (eventKey == 0 || !Keyboard.getEventKeyState() || ModuleManager.mc.currentScreen instanceof CascadeGui) {
            return;
        }
        this.mods.forEach(module -> {
            if (module.getBind().getKey() == eventKey) {
                module.toggle();
            }
        });
    }
}

