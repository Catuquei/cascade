package cascade.features.modules.visual;

import cascade.event.events.PacketEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.core.Pair;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.BossInfoClient;
import net.minecraft.client.gui.GuiBossOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.world.BossInfo;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class NoRender extends Module {

    public NoRender() {
        super("NoRender", Category.VISUAL, "stops certain things from rendering");
        INSTANCE = this;
    }

    public Setting<Boolean> noMaps = register(new Setting("Maps", true));
    //public Setting<Boolean> cancelMaps = register(new Setting("CancelMaps", false, v -> noMaps.getValue()));
    Setting<Boolean> smartFPS = register(new Setting("SmartFPS", false));
    Setting<Boolean> noFallingBlocks = register(new Setting("NoFallingBlocks", false));
    public Setting<Boolean> noHurt = register(new Setting("Hurt", true));
    public Setting<Boolean> noOverlay = register(new Setting("Overlay", true));
    public Setting<Boolean> totemPops = register(new Setting("TotemPop", false));
    public Setting<Boolean> noArmor = register(new Setting("Armor", true));
    public Setting<Boolean> noAdvancements = register(new Setting("Advancement", true));
    public Setting<Boss> boss = register(new Setting("BossBars", Boss.None));
    public enum Boss {None, Remove, Stack, Minimize}
    public Setting<Float> scale = register(new Setting("Scale", 0.5f, 0.0f, 1.0f, v -> boss.getValue() == Boss.Minimize || boss.getValue() != Boss.Stack));
    Set<Integer> ids = new HashSet<>();
    private static NoRender INSTANCE;

    public static NoRender getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoRender();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (noFallingBlocks.getValue()) {
            for (Entity e : mc.world.loadedEntityList) {
                if (e instanceof EntityFallingBlock && !e.isDead) {
                    e.setDead();
                }
            }
        }
        /*for (Entity e : mc.world.loadedEntityList) {
            if (e instanceof EntityItem && !e.isDead && !mc.player.canEntityBeSeen(e)) {
                if (smartFPS.getValue()) {
                    e.setDead();
                    ids.add(e.getEntityId());
                }
                if (!smartFPS.getValue() && !ids.isEmpty())
                    ids.forEach(this::revive);
                    ids.clear();
                }
            }
        }*/
    }

    /*oid revive(int id) {
        ids.remove(id);
        entity.isDead = false;
        mc.world.addEntityToWorld(entity.getEntityId(), entity);
        entity.isDead = false;
    }*/

    @SubscribeEvent
    public void onRenderPre(RenderGameOverlayEvent.Pre e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && boss.getValue() != Boss.None && isEnabled()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post e) {
        if (e.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && boss.getValue() != Boss.None && isEnabled()) {
            if (boss.getValue() == Boss.Minimize) {
                Map<UUID, BossInfoClient> map = mc.ingameGUI.getBossOverlay().mapBossInfos;
                if (map == null) {
                    return;
                }
                ScaledResolution scaledresolution = new ScaledResolution(mc);
                int i = scaledresolution.getScaledWidth();
                int j = 12;
                for (Map.Entry<UUID, BossInfoClient> entry : map.entrySet()) {
                     BossInfoClient info = entry.getValue();
                    String text = info.getName().getFormattedText();
                    int k = (int)(i / scale.getValue() / 2.0f - 91.0f);
                    GL11.glScaled((double)scale.getValue(), (double)scale.getValue(), 1.0);
                    if (!e.isCanceled()) {
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        mc.getTextureManager().bindTexture(GuiBossOverlay.GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k, j, (BossInfo)info);
                        mc.fontRenderer.drawStringWithShadow(text, i / scale.getValue() / 2.0f - mc.fontRenderer.getStringWidth(text) / 2, (float)(j - 9), 16777215);
                    }
                    GL11.glScaled(1.0 / scale.getValue(), 1.0 / scale.getValue(), 1.0);
                    j += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            } else if (boss.getValue() == Boss.Stack) {
                Map<UUID, BossInfoClient> map = mc.ingameGUI.getBossOverlay().mapBossInfos;
                HashMap<String, Pair<BossInfoClient, Integer>> to = new HashMap<String, Pair<BossInfoClient, Integer>>();
                for (Map.Entry<UUID, BossInfoClient> entry2 : map.entrySet()) {
                    String s = entry2.getValue().getName().getFormattedText();
                    if (to.containsKey(s)) {
                        Pair<BossInfoClient, Integer> p = to.get(s);
                        p = new Pair<>(p.getKey(), p.getValue() + 1);
                        to.put(s, p);
                    } else {
                        Pair<BossInfoClient, Integer> p = new Pair<BossInfoClient, Integer>(entry2.getValue(), 1);
                        to.put(s, p);
                    }
                }
                ScaledResolution scaledresolution2 = new ScaledResolution(mc);
                int l = scaledresolution2.getScaledWidth();
                int m = 12;
                for (Map.Entry<String, Pair<BossInfoClient, Integer>> entry3 : to.entrySet()) {
                    String text = entry3.getKey();
                    BossInfoClient info2 = entry3.getValue().getKey();
                    int a = entry3.getValue().getValue();
                    text = text + " x" + a;
                    int k2 = (int)(l / scale.getValue() / 2.0f - 91.0f);
                    GL11.glScaled((double)scale.getValue(), (double)scale.getValue(), 1.0);
                    if (!e.isCanceled()) {
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        mc.getTextureManager().bindTexture(GuiBossOverlay.GUI_BARS_TEXTURES);
                        mc.ingameGUI.getBossOverlay().render(k2, m, (BossInfo)info2);
                        mc.fontRenderer.drawStringWithShadow(text, l / scale.getValue() / 2.0f - mc.fontRenderer.getStringWidth(text) / 2, (float)(m - 9), 16777215);
                    }
                    GL11.glScaled(1.0 / scale.getValue(), 1.0 / scale.getValue(), 1.0);
                    m += 10 + mc.fontRenderer.FONT_HEIGHT;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Receive e) {
        if (e.getPacket() instanceof SPacketMaps && noMaps.getValue() && isEnabled()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent e) {
        if (!fullNullCheck() && isEnabled()) {
            if ((e.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER || e.getOverlayType() == RenderBlockOverlayEvent.OverlayType.BLOCK) && noOverlay.getValue()) {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void something(EntityViewRenderEvent.FogDensity e) {
        if (noOverlay.getValue() && isEnabled()) {
            if (e.getState().getMaterial().equals(Material.WATER) || e.getState().getMaterial().equals(Material.LAVA)) {
                e.setDensity(0);
                e.setCanceled(true);
            }
        }
    }
}