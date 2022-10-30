package cascade.features.modules.hud;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.features.modules.Module;
import cascade.features.modules.core.ClickGui;
import cascade.features.setting.Setting;
import cascade.util.player.InventoryUtil;
import cascade.util.render.ColorUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class PvpInfo extends Module {

    public PvpInfo() {
        super("PvpInfo", Category.HUD, "");
    }

    Setting<String> text = register(new Setting("Text", "Cascade"));

    public void onRender2D(Render2DEvent e) {
        if (fullNullCheck()) {
            return;
        }

        renderer.drawString(text.getValue(), 2.0F, 250, HUDManager.INSTANCE.getColor(), true);

        int totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> (itemStack.getItem() == Items.TOTEM_OF_UNDYING)).mapToInt(ItemStack::getCount).sum() + (InventoryUtil.heldItem(Items.TOTEM_OF_UNDYING, InventoryUtil.Hand.Off) ? 1 : 0);
        renderer.drawString(totems == 0 ? ChatFormatting.RED + "" + totems : ChatFormatting.GREEN + "" +  totems, 2, 260, HUDManager.INSTANCE.getColor(), true);

        int ping = Cascade.serverManager.getPing();
        String pingString = null;
        if (ping <= 50) {
            pingString = ChatFormatting.GREEN + "" + ping;
        }
        if (ping > 50 && ping <= 100) {
            pingString = ChatFormatting.YELLOW + "" + ping;
        }
        if (ping > 100) {
            pingString = ChatFormatting.RED + "" + ping;
        }
        renderer.drawString(pingString, 2.0F, 270, HUDManager.INSTANCE.getColor(), true);
    }
}