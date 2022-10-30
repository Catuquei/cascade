package cascade;

import cascade.features.modules.core.ClientManagement;
import cascade.manager.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(modid = "cascade", name = Cascade.MODNAME, version = Cascade.MODVER)
public class Cascade {

    public static final String MODNAME = "Cascade";
    public static final String MODVER = "0.2.7";
    public static final Logger LOGGER = LogManager.getLogger("Cascade");
    public static ChatManager chatManager;
    public static FriendManager friendManager;
    public static ModuleManager moduleManager;
    public static ColorManager colorManager;
    public static InventoryManager inventoryManager;
    public static PacketManager packetManager;
    public static PotionManager potionManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static SpeedManager speedManager;
    public static SwapManager swapManager;
    public static ConfigManager configManager;
    public static ServerManager serverManager;
    public static EventManager eventManager;
    public static TimerManager timerManager;
    public static TextManager textManager;
    public static Minecraft mc = Minecraft.getMinecraft();

    @Instance
    public static Cascade INSTANCE;
    private static boolean unloaded = false;

    public static void load() {
        LOGGER.info("\n\nLoading " + MODNAME);
        unloaded = false;
        textManager = new TextManager();
        chatManager = new ChatManager();
        friendManager = new FriendManager();
        moduleManager = new ModuleManager();
        rotationManager = new RotationManager();
        eventManager = new EventManager();
        timerManager = new TimerManager();
        speedManager = new SpeedManager();
        swapManager = new SwapManager();
        potionManager = new PotionManager();
        inventoryManager = new InventoryManager();
        packetManager = new PacketManager();
        serverManager = new ServerManager();
        colorManager = new ColorManager();
        positionManager = new PositionManager();
        configManager = new ConfigManager();
        moduleManager.init();
        configManager.init();
        eventManager.init();
        textManager.init(true);
        moduleManager.onLoad();
        packetManager.load();
        swapManager.load();
        timerManager.load();
        LOGGER.info(MODNAME + " successfully loaded!\n");
    }

    public static void onUnload() {
        if (!unloaded) {
            eventManager.onUnload();
            timerManager.unload();
            moduleManager.onUnload();
            configManager.saveConfig(configManager.config.replaceFirst("cascade/", ""));
            moduleManager.onUnloadPost();
            packetManager.unload();
            swapManager.unload();
            timerManager.unload();
            unloaded = true;
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent e) {
        Display.setTitle(ClientManagement.getInstance().title.getValueAsString() == null ? MODNAME + " " + MODVER : ClientManagement.getInstance().title.getValueAsString());
        load();
    }
}