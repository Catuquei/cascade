package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render3DEvent;
import cascade.features.gui.font.CustomFont;
import cascade.features.modules.Module;
import cascade.features.modules.core.FontMod;
import cascade.features.setting.Setting;
import cascade.util.entity.EntityUtil;
import cascade.util.render.ColorUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Nametags extends Module {

    public Nametags() {
        super("Nametags", Category.VISUAL, "Displays info above players");
        this.customFont = new CustomFont(new Font("Verdana", 0, 17), true, true);
        this.glCapMap = new HashMap<Integer, Boolean>();
        this.camera = (ICamera)new Frustum();
        this.outline = (Setting<Boolean>)this.register(new Setting("Outline", true));
        this.inside = (Setting<Boolean>)this.register(new Setting("Background", true));
        this.outlineMode = (Setting<OutLineMode>)this.register(new Setting("Outline Mode", OutLineMode.DEPEND));
        this.Ored = (Setting<Integer>)this.register(new Setting("Outline Red", 0, 0, 255, v -> this.outline.getValue() && this.outlineMode.getValue() == OutLineMode.NORMAL));
        this.Ogreen = (Setting<Integer>)this.register(new Setting("Outline Green", 0, 0, 255, v -> this.outline.getValue() && this.outlineMode.getValue() == OutLineMode.NORMAL));
        this.Oblue = (Setting<Integer>)this.register(new Setting("Outline Blue", 0, 0, 255, v -> this.outline.getValue() && this.outlineMode.getValue() == OutLineMode.NORMAL));
        this.Oalpha = (Setting<Integer>)this.register(new Setting("Outline Alpha", 155, 0, 255, v -> this.outline.getValue()));
        this.Owidth = (Setting<Float>)this.register(new Setting("Outline Width", 1.5f, 0.0f, 3.0f, v -> this.outline.getValue()));
        this.reversed = (Setting<Boolean>)this.register(new Setting("Reversed", false));
        this.reversedHand = (Setting<Boolean>)this.register(new Setting("Reversed Hand", false));
        this.enchantMode = (Setting<EnchantMode>)this.register(new Setting("Enchant Mode", EnchantMode.MAX));
        this.health = (Setting<Boolean>)this.register(new Setting("Health", true));
        this.gameMode = (Setting<Boolean>)this.register(new Setting("GameMode", true));
        this.ping = (Setting<Boolean>)this.register(new Setting("Ping", true));
        this.pingColor = (Setting<Boolean>)this.register(new Setting("Ping Color", true));
        this.armor = (Setting<Boolean>)this.register(new Setting("Armor", true));
        this.durability = (Setting<Boolean>)this.register(new Setting("Durability", true));
        this.item = (Setting<Boolean>)this.register(new Setting("Item Name", true));
        this.invisibles = (Setting<Boolean>)this.register(new Setting("Invisibles", false));
        this.scale = (Setting<Float>)this.register(new Setting("Scale", 5.0f, 1.0f, 9.0f));
        this.height = (Setting<Float>)this.register(new Setting("Height", 2.5f, 0.5f, 5.0f));
        this.friends = (Setting<Boolean>)this.register(new Setting("Friends", true));
        this.friendMode = (Setting<FriendMode>)this.register(new Setting("Friend Mode", FriendMode.TEXT, v -> this.friends.getValue()));
        this.red = (Setting<Integer>)this.register(new Setting("Friend Red", 0, 0, 255, v -> this.friends.getValue()));
        this.green = (Setting<Integer>)this.register(new Setting("Friend Green", 130, 0, 255, v -> this.friends.getValue()));
        this.blue = (Setting<Integer>)this.register(new Setting("Friend Blue", 130, 0, 255, v -> this.friends.getValue()));
        INSTANCE = this;
    }

    private final Setting<Boolean> outline;
    private final Setting<Boolean> inside;
    private final Setting<Integer> Ored;
    private final Setting<Integer> Ogreen;
    private final Setting<Integer> Oblue;
    private final Setting<Integer> Oalpha;
    private final Setting<Float> Owidth;
    private final Setting<EnchantMode> enchantMode;
    public enum EnchantMode {PROT, LIST, MAX, NONE;}
    private final Setting<Boolean> reversed;
    private final Setting<Boolean> reversedHand;
    private final Setting<Boolean> health;
    private final Setting<Boolean> gameMode;
    private final Setting<Boolean> ping;
    private final Setting<Boolean> pingColor;
    private final Setting<Boolean> armor;
    private final Setting<Boolean> durability;
    private final Setting<Boolean> item;
    private final Setting<Boolean> invisibles;
    private final Setting<Float> scale;
    private final Setting<Float> height;
    private final Setting<FriendMode> friendMode;
    public enum FriendMode {TEXT, BOX;}
    private final Setting<Boolean> friends;
    private final Setting<Integer> red;
    private final Setting<Integer> green;
    private final Setting<Integer> blue;
    private final CustomFont customFont;
    public Setting<OutLineMode> outlineMode;
    public enum OutLineMode {NORMAL, DEPEND, RAINBOW, RAINBOW2;}
    private ICamera camera;
    boolean shownItem;
    private Map<Integer, Boolean> glCapMap;
    private static Nametags INSTANCE;

    public static Nametags getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Nametags();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (fullNullCheck() || event == null) {
            return;
        }
        final EntityPlayerSP entityPlayerSP = (EntityPlayerSP)((Nametags.mc.getRenderViewEntity() == null) ? Nametags.mc.player : Nametags.mc.getRenderViewEntity());
        final double d3 = entityPlayerSP.lastTickPosX + (entityPlayerSP.posX - entityPlayerSP.lastTickPosX) * event.getPartialTicks();
        final double d4 = entityPlayerSP.lastTickPosY + (entityPlayerSP.posY - entityPlayerSP.lastTickPosY) * event.getPartialTicks();
        final double d5 = entityPlayerSP.lastTickPosZ + (entityPlayerSP.posZ - entityPlayerSP.lastTickPosZ) * event.getPartialTicks();
        this.camera.setPosition(d3, d4, d5);
        final java.util.List<EntityPlayer> players = new ArrayList<EntityPlayer>(Nametags.mc.world.playerEntities);
        players.sort(Comparator.comparing(entityPlayer -> entityPlayerSP.getDistance((Entity)entityPlayer)).reversed());
        for (final EntityPlayer p : players) {
            final NetworkPlayerInfo npi = Nametags.mc.player.connection.getPlayerInfo(p.getGameProfile().getId());
            if (!this.camera.isBoundingBoxInFrustum(p.getEntityBoundingBox()) && !this.camera.isBoundingBoxInFrustum(p.getEntityBoundingBox().offset(0.0, 2.0, 0.0))) {
                continue;
            }
            if (p == Nametags.mc.getRenderViewEntity()) {
                continue;
            }
            if (!p.isEntityAlive()) {
                continue;
            }
            final double pX = p.lastTickPosX + (p.posX - p.lastTickPosX) * Nametags.mc.timer.renderPartialTicks - Nametags.mc.renderManager.renderPosX;
            final double pY = p.lastTickPosY + (p.posY - p.lastTickPosY) * Nametags.mc.timer.renderPartialTicks - Nametags.mc.renderManager.renderPosY;
            final double pZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * Nametags.mc.timer.renderPartialTicks - Nametags.mc.renderManager.renderPosZ;
            if (npi != null && this.getShortName(npi.getGameType().getName()).equalsIgnoreCase("SP") && !this.invisibles.getValue()) {
                continue;
            }
            if (p.getName().startsWith("Body #")) {
                continue;
            }
            try {
                this.renderNametag(p, pX, pY, pZ);
            } catch(Exception ex) {
                Cascade.LOGGER.info("Caught an exception from Nametags");
                ex.printStackTrace();
            }
        }
    }

    public String getShortName(final String gameType) {
        if (gameType.equalsIgnoreCase("survival")) {
            return "S";
        }
        if (gameType.equalsIgnoreCase("creative")) {
            return "C";
        }
        if (gameType.equalsIgnoreCase("adventure")) {
            return "A";
        }
        if (gameType.equalsIgnoreCase("spectator")) {
            return "SP";
        }
        return "NONE";
    }

    public String getHealth(final float health) {
        if (health > 18.0f) {
            return "a";
        }
        if (health > 16.0f) {
            return "2";
        }
        if (health > 12.0f) {
            return "e";
        }
        if (health > 8.0f) {
            return "6";
        }
        if (health > 5.0f) {
            return "c";
        }
        return "4";
    }

    public String getPing(final float ping) {
        if (ping > 200.0f) {
            return "c";
        }
        if (ping > 100.0f) {
            return "e";
        }
        return "a";
    }

    private String getName(final EntityPlayer player) {
        return player.getName();
    }

    public void renderNametag(final EntityPlayer player, final double x, final double y, final double z) {
        this.shownItem = false;
        GlStateManager.pushMatrix();
        final NetworkPlayerInfo npi = Nametags.mc.player.connection.getPlayerInfo(player.getGameProfile().getId());
        final boolean isFriend = Cascade.friendManager.isFriend(player.getName()) && this.friends.getValue();
        final StringBuilder append = new StringBuilder().append((isFriend && this.friendMode.getValue() == FriendMode.TEXT) ? ("§" + (isFriend ? "b" : "c")) : (player.isSneaking() ? "§7" : "§r")).append(this.getName(player)).append((this.gameMode.getValue() && npi != null) ? (" [" + this.getShortName(npi.getGameType().getName()) + "]") : "").append((this.ping.getValue() && npi != null) ? (" " + (this.pingColor.getValue() ? ("§" + this.getPing((float) npi.getResponseTime())) : "") + npi.getResponseTime() + "ms") : "").append(this.health.getValue() ? (" §" + this.getHealth(player.getHealth() + player.getAbsorptionAmount()) + MathHelper.ceil(player.getHealth() + player.getAbsorptionAmount())) : "");
        String sting = "";
        final EntityPlayerSP entityPlayerSP = (EntityPlayerSP)((Nametags.mc.getRenderViewEntity() == null) ? Nametags.mc.player : Nametags.mc.getRenderViewEntity());
        final float distance = entityPlayerSP.getDistance(player);
        float var14 = ((distance / 5.0f <= 2.0f) ? 2.0f : (distance / 5.0f * (this.scale.getValue() / 100.0f * 10.0f + 1.0f))) * 2.5f * (this.scale.getValue() / 100.0f / 10.0f);
        if (distance <= 8.0) {
            var14 = 0.0245f;
        }
        GL11.glTranslated((double)(float)x, (float)y + this.height.getValue() - (player.isSneaking() ? 0.4 : 0.0) + ((distance / 5.0f > 2.0f) ? (distance / 12.0f - 0.7) : 0.0), (double)(float)z);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glRotatef(-Nametags.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(Nametags.mc.getRenderManager().playerViewX, (Nametags.mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0.0f, 0.0f);
        GL11.glScalef(-var14, -var14, var14);
        this.disableGlCap(2896, 2929);
        this.enableGlCap(3042);
        GL11.glBlendFunc(770, 771);
        int width;
        if (FontMod.getInstance().isOn()) {
            width = this.customFont.getStringWidth(name) / 2 + 1;
        }
        else {
            width = Nametags.mc.fontRenderer.getStringWidth(name) / 2 + 1;
        }
        final int color = (isFriend && this.friendMode.getValue() == FriendMode.BOX) ? new Color(this.red.getValue(), this.green.getValue(), this.blue.getValue()).getRGB() : 0;
        int outlineColor = new Color(this.Ored.getValue(), this.Ogreen.getValue(), this.Oblue.getValue(), this.Oalpha.getValue()).getRGB();
        if (this.outlineMode.getValue() == OutLineMode.DEPEND) {
            if (Cascade.friendManager.isFriend(player.getName())) {
                outlineColor = new Color(0, 191, 230, this.Oalpha.getValue()).getRGB();
            }
            else if (EntityUtil.isBurrow((Entity)player)) {
                outlineColor = new Color(177, 27, 196, this.Oalpha.getValue()).getRGB();
            }
            else if (EntityUtil.isSafe((Entity)player)) {
                outlineColor = new Color(0, 255, 0, this.Oalpha.getValue()).getRGB();
            }
            else {
                outlineColor = new Color(255, 0, 0, this.Oalpha.getValue()).getRGB();
            }
        }
        if (this.inside.getValue()) {
            Gui.drawRect(-width - 1, 8, width + 1, 19, changeAlpha(color, 120));
        }
        if (this.outline.getValue()) {
            this.drawOutlineLine(-width - 1, 8.0, width + 1, 19.0, this.Owidth.getValue(), outlineColor);
        }
        if (FontMod.getInstance().isOn()) {
            this.customFont.drawStringWithShadow(name, -width, 8.649999618530273, -1);
        }
        else {
            Nametags.mc.fontRenderer.drawStringWithShadow(name, (float)(-width), 9.2f, -1);
        }
        if (this.armor.getValue()) {
            int xOffset = -8;
            final Item mainhand = player.getHeldItemMainhand().getItem();
            final Item offhand = player.getHeldItemOffhand().getItem();
            if (mainhand != Items.AIR && offhand == Items.AIR) {
                xOffset = -16;
            }
            else if (mainhand == Items.AIR && offhand != Items.AIR) {
                xOffset = 0;
            }
            int count = 0;
            for (final ItemStack armourStack : player.inventory.armorInventory) {
                if (armourStack != null) {
                    xOffset -= 8;
                    if (armourStack.getItem() == Items.AIR) {
                        continue;
                    }
                    ++count;
                }
            }
            if (player.getHeldItemOffhand().getItem() != Items.AIR) {
                ++count;
            }
            final int cacheX = xOffset - 8;
            xOffset += 8 * (5 - count) - ((count == 0) ? 4 : 0);
            Label_1638: {
                Label_1617: {
                    if (this.reversedHand.getValue()) {
                        if (player.getHeldItemOffhand().getItem() == Items.AIR) {
                            break Label_1617;
                        }
                    }
                    else if (player.getHeldItemMainhand().getItem() == Items.AIR) {
                        break Label_1617;
                    }
                    xOffset -= 10;
                    if (this.reversedHand.getValue()) {
                        final ItemStack renderStack = player.getHeldItemOffhand().copy();
                        this.renderItem(player, renderStack, xOffset, -8, cacheX, false);
                    }
                    else {
                        final ItemStack renderStack = player.getHeldItemMainhand().copy();
                        this.renderItem(player, renderStack, xOffset, -8, cacheX, true);
                    }
                    xOffset += 18;
                    break Label_1638;
                }
                if (!this.reversedHand.getValue()) {
                    this.shownItem = true;
                }
            }
            if (this.reversed.getValue()) {
                for (int index = 0; index <= 3; ++index) {
                    final ItemStack armourStack2 = (ItemStack)player.inventory.armorInventory.get(index);
                    if (armourStack2 != null && armourStack2.getItem() != Items.AIR) {
                        final ItemStack renderStack2 = armourStack2.copy();
                        this.renderItem(player, renderStack2, xOffset, -8, cacheX, false);
                        xOffset += 16;
                    }
                }
            }
            else {
                for (int index = 3; index >= 0; --index) {
                    final ItemStack armourStack2 = (ItemStack)player.inventory.armorInventory.get(index);
                    if (armourStack2 != null && armourStack2.getItem() != Items.AIR) {
                        final ItemStack renderStack2 = armourStack2.copy();
                        this.renderItem(player, renderStack2, xOffset, -8, cacheX, false);
                        xOffset += 16;
                    }
                }
            }
            Label_1919: {
                if (this.reversedHand.getValue()) {
                    if (player.getHeldItemMainhand().getItem() == Items.AIR) {
                        break Label_1919;
                    }
                }
                else if (player.getHeldItemOffhand().getItem() == Items.AIR) {
                    break Label_1919;
                }
                xOffset += 0;
                if (this.reversedHand.getValue()) {
                    final ItemStack renderOffhand = player.getHeldItemMainhand().copy();
                    this.renderItem(player, renderOffhand, xOffset, -8, cacheX, true);
                }
                else {
                    final ItemStack renderOffhand = player.getHeldItemOffhand().copy();
                    this.renderItem(player, renderOffhand, xOffset, -8, cacheX, false);
                }
                xOffset += 8;
            }
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
        }
        else if (this.durability.getValue()) {
            int xOffset = -6;
            int count2 = 0;
            for (final ItemStack armourStack3 : player.inventory.armorInventory) {
                if (armourStack3 != null) {
                    xOffset -= 8;
                    if (armourStack3.getItem() == Items.AIR) {
                        continue;
                    }
                    ++count2;
                }
            }
            if (player.getHeldItemOffhand().getItem() != Items.AIR) {
                ++count2;
            }
            final int cacheX2 = xOffset - 8;
            xOffset += 8 * (5 - count2) - ((count2 == 0) ? 4 : 0);
            if (this.reversed.getValue()) {
                for (int index2 = 0; index2 <= 3; ++index2) {
                    final ItemStack armourStack4 = (ItemStack)player.inventory.armorInventory.get(index2);
                    if (armourStack4 != null && armourStack4.getItem() != Items.AIR) {
                        final ItemStack renderStack3 = armourStack4.copy();
                        this.renderDurabilityText(player, renderStack3, xOffset, -8);
                        xOffset += 16;
                    }
                }
            }
            else {
                for (int index2 = 3; index2 >= 0; --index2) {
                    final ItemStack armourStack4 = (ItemStack)player.inventory.armorInventory.get(index2);
                    if (armourStack4 != null && armourStack4.getItem() != Items.AIR) {
                        final ItemStack renderStack3 = armourStack4.copy();
                        this.renderDurabilityText(player, renderStack3, xOffset, -8);
                        xOffset += 16;
                    }
                }
            }
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
        this.resetCaps();
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();
    }

    public float getNametagSize(final EntityLivingBase player) {
        final ScaledResolution scaledRes = new ScaledResolution(Nametags.mc);
        final double twoDscale = scaledRes.getScaleFactor() / Math.pow(scaledRes.getScaleFactor(), 2.0);
        final EntityPlayerSP entityPlayerSP = (EntityPlayerSP)((Nametags.mc.getRenderViewEntity() == null) ? Nametags.mc.player : Nametags.mc.getRenderViewEntity());
        return (float)twoDscale + entityPlayerSP.getDistance((Entity)player) / 7.0f;
    }

    public void drawBorderRect(final float left, final float top, final float right, final float bottom, final int bcolor, final int icolor, final float f) {
        drawGuiRect(left + f, top + f, right - f, bottom - f, icolor);
        drawGuiRect(left, top, left + f, bottom, bcolor);
        drawGuiRect(left + f, top, right, top + f, bcolor);
        drawGuiRect(left + f, bottom - f, right, bottom, bcolor);
        drawGuiRect(right - f, top + f, right, bottom - f, bcolor);
    }

    public static void drawGuiRect(final double x1, final double y1, final double x2, final double y2, final int color) {
        final float red = (color >> 24 & 0xFF) / 255.0f;
        final float green = (color >> 16 & 0xFF) / 255.0f;
        final float blue = (color >> 8 & 0xFF) / 255.0f;
        final float alpha = (color & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(green, blue, alpha, red);
        GL11.glBegin(7);
        GL11.glVertex2d(x2, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }

    public static void fakeGuiRect(double left, double top, double right, double bottom, final int color) {
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }
        final float f3 = (color >> 24 & 0xFF) / 255.0f;
        final float f4 = (color >> 16 & 0xFF) / 255.0f;
        final float f5 = (color >> 8 & 0xFF) / 255.0f;
        final float f6 = (color & 0xFF) / 255.0f;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f4, f5, f6, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0).endVertex();
        bufferbuilder.pos(right, bottom, 0.0).endVertex();
        bufferbuilder.pos(right, top, 0.0).endVertex();
        bufferbuilder.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawBorderedRect(final double x, final double y, final double x1, final double y1, final double width, final int internalColor, final int borderColor) {
        GlStateManager.pushMatrix();
        enableGL2D();
        fakeGuiRect(x + width, y + width, x1 - width, y1 - width, internalColor);
        fakeGuiRect(x + width, y, x1 - width, y + width, borderColor);
        fakeGuiRect(x, y, x + width, y1, borderColor);
        fakeGuiRect(x1 - width, y, x1, y1, borderColor);
        fakeGuiRect(x + width, y1 - width, x1 - width, y1, borderColor);
        disableGL2D();
        GlStateManager.popMatrix();
    }

    public void renderItem(final EntityPlayer player, final ItemStack stack, final int x, final int y, final int nameX, final boolean showHeldItemText) {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.clear(256);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        Nametags.mc.getRenderItem().zLevel = -100.0f;
        GlStateManager.scale(1.0f, 1.0f, 0.01f);
        Nametags.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y / 2 - 12);
        if (this.durability.getValue()) {
            Nametags.mc.getRenderItem().renderItemOverlays(Nametags.mc.fontRenderer, stack, x, y / 2 - 12);
        }
        Nametags.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.scale(1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.disableDepth();
        this.renderEnchantText(player, stack, x, y - 18);
        if (!this.shownItem && this.item.getValue() && showHeldItemText) {
            if (FontMod.getInstance().isOn()) {
                this.customFont.drawString(stack.getDisplayName().equalsIgnoreCase("Air") ? "" : stack.getDisplayName(), nameX * 2 + 95 - this.customFont.getStringWidth(stack.getDisplayName()) / 2, y - 37, -1, true);
            }
            else {
                Nametags.mc.fontRenderer.drawStringWithShadow(stack.getDisplayName().equalsIgnoreCase("Air") ? "" : stack.getDisplayName(), (float)(nameX * 2 + 95 - Nametags.mc.fontRenderer.getStringWidth(stack.getDisplayName()) / 2), (float)(y - 37), -1);
            }
            this.shownItem = true;
        }
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GL11.glPopMatrix();
    }

    public boolean isMaxEnchants(final ItemStack stack) {
        final NBTTagList enchants = stack.getEnchantmentTagList();
        final List<String> enchantments = new ArrayList<String>();
        int count = 0;
        if (enchants == null) {
            return false;
        }
        for (int index = 0; index < enchants.tagCount(); ++index) {
            final short id = enchants.getCompoundTagAt(index).getShort("id");
            final short level = enchants.getCompoundTagAt(index).getShort("lvl");
            final Enchantment enc = Enchantment.getEnchantmentByID((int)id);
            if (enc != null) {
                enchantments.add(enc.getTranslatedName((int)level));
            }
        }
        if (stack.getItem() == Items.DIAMOND_HELMET) {
            final int maxnum = 5;
            for (final String s : enchantments) {
                if (s.equalsIgnoreCase("Protection IV")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Respiration III")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Aqua Affinity")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Unbreaking III")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Mending")) {
                    ++count;
                }
            }
            return count >= 5;
        }
        if (stack.getItem() == Items.DIAMOND_CHESTPLATE) {
            final int maxnum = 3;
            for (final String s : enchantments) {
                if (s.equalsIgnoreCase("Protection IV")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Unbreaking III")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Mending")) {
                    ++count;
                }
            }
            return count >= 3;
        }
        if (stack.getItem() == Items.DIAMOND_LEGGINGS) {
            final int maxnum = 3;
            for (final String s : enchantments) {
                if (s.equalsIgnoreCase("Blast Protection IV")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Unbreaking III")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Mending")) {
                    ++count;
                }
            }
            return count >= 3;
        }
        if (stack.getItem() == Items.DIAMOND_BOOTS) {
            final int maxnum = 5;
            for (final String s : enchantments) {
                if (s.equalsIgnoreCase("Protection IV")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Feather Falling IV")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Depth Strider III")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Unbreaking III")) {
                    ++count;
                }
                if (s.equalsIgnoreCase("Mending")) {
                    ++count;
                }
            }
            return count >= 5;
        }
        return false;
    }

    private void renderDurabilityText(final EntityPlayer player, final ItemStack stack, final int x, final int y) {
        GL11.glPushMatrix();
        GL11.glDepthMask(true);
        GlStateManager.clear(256);
        GlStateManager.disableDepth();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.scale(1.0f, 1.0f, 0.01f);
        GlStateManager.scale(1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5, 0.5, 0.5);
        GlStateManager.disableDepth();
        if (stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) {
            final float green = (stack.getMaxDamage() - (float)stack.getItemDamage()) / stack.getMaxDamage();
            final float red = 1.0f - green;
            final int dmg = 100 - (int)(red * 100.0f);
            if (FontMod.getInstance().isOn()) {
                this.customFont.drawStringWithShadow(dmg + "%", x * 2 + 4, y - 10, ColorUtil.ColorHolder.toHex((int)(red * 255.0f), (int)(green * 255.0f), 0));
            }
            else {
                Nametags.mc.fontRenderer.drawStringWithShadow(dmg + "%", (float)(x * 2 + 4), (float)(y - 10), ColorUtil.ColorHolder.toHex((int)(red * 255.0f), (int)(green * 255.0f), 0));
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GL11.glPopMatrix();
    }

    public void renderEnchantText(final EntityPlayer player, final ItemStack stack, final int x, final int y) {
        int encY = y;
        int yCount = y;
        if ((stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemTool) && this.durability.getValue()) {
            final float green = (stack.getMaxDamage() - (float)stack.getItemDamage()) / stack.getMaxDamage();
            final float red = 1.0f - green;
            final int dmg = 100 - (int)(red * 100.0f);
            if (FontMod.getInstance().isOn()) {
                this.customFont.drawStringWithShadow(dmg + "%", x * 2 + 4, y - 10, ColorUtil.ColorHolder.toHex((int)(red * 255.0f), (int)(green * 255.0f), 0));
            }
            else {
                Nametags.mc.fontRenderer.drawStringWithShadow(dmg + "%", (float)(x * 2 + 4), (float)(y - 10), ColorUtil.ColorHolder.toHex((int)(red * 255.0f), (int)(green * 255.0f), 0));
            }
        }
        if (this.enchantMode.getValue() == EnchantMode.NONE) {
            return;
        }
        if (this.enchantMode.getValue() == EnchantMode.MAX && this.isMaxEnchants(stack)) {
            GL11.glPushMatrix();
            GL11.glScalef(1.0f, 1.0f, 0.0f);
            if (FontMod.getInstance().isOn()) {
                this.customFont.drawStringWithShadow("Max", x * 2 + 7, yCount + 24, 16711680);
            }
            else {
                Nametags.mc.fontRenderer.drawStringWithShadow("Max", (float)(x * 2 + 7), (float)(yCount + 24), 16711680);
            }
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            GL11.glPopMatrix();
            return;
        }
        if (this.enchantMode.getValue() == EnchantMode.PROT) {
            final NBTTagList enchants = stack.getEnchantmentTagList();
            if (enchants != null) {
                for (int index = 0; index < enchants.tagCount(); ++index) {
                    final short id = enchants.getCompoundTagAt(index).getShort("id");
                    final short level = enchants.getCompoundTagAt(index).getShort("lvl");
                    final Enchantment enc = Enchantment.getEnchantmentByID((int)id);
                    if (enc != null && !enc.isCurse()) {
                        String encName = (level == 1) ? enc.getTranslatedName((int)level).substring(0, 3).toLowerCase() : (enc.getTranslatedName((int)level).substring(0, 2).toLowerCase() + level);
                        encName = encName.substring(0, 1).toUpperCase() + encName.substring(1);
                        if (encName.contains("Pr") || encName.contains("Bl")) {
                            GL11.glPushMatrix();
                            GL11.glScalef(1.0f, 1.0f, 0.0f);
                            if (FontMod.getInstance().isOn()) {
                                this.customFont.drawStringWithShadow(encName, x * 2 + 3, yCount, -1);
                            }
                            else {
                                Nametags.mc.fontRenderer.drawStringWithShadow(encName, (float)(x * 2 + 3), (float)yCount, -1);
                            }
                            GL11.glScalef(1.0f, 1.0f, 1.0f);
                            GL11.glPopMatrix();
                            encY += 8;
                            yCount += 8;
                        }
                    }
                }
            }
            return;
        }
        final NBTTagList enchants = stack.getEnchantmentTagList();
        if (enchants != null) {
            for (int index = 0; index < enchants.tagCount(); ++index) {
                final short id = enchants.getCompoundTagAt(index).getShort("id");
                final short level = enchants.getCompoundTagAt(index).getShort("lvl");
                final Enchantment enc = Enchantment.getEnchantmentByID((int)id);
                if (enc != null && !enc.isCurse()) {
                    String encName = (level == 1) ? enc.getTranslatedName((int)level).substring(0, 3).toLowerCase() : (enc.getTranslatedName((int)level).substring(0, 2).toLowerCase() + level);
                    encName = encName.substring(0, 1).toUpperCase() + encName.substring(1);
                    GL11.glPushMatrix();
                    GL11.glScalef(1.0f, 1.0f, 0.0f);
                    if (FontMod.getInstance().isEnabled()) {
                        this.customFont.drawStringWithShadow(encName, x * 2 + 3, yCount, -1);
                    }
                    else {
                        Nametags.mc.fontRenderer.drawStringWithShadow(encName, (float)(x * 2 + 3), (float)yCount, -1);
                    }
                    GL11.glScalef(1.0f, 1.0f, 1.0f);
                    GL11.glPopMatrix();
                    encY += 8;
                    yCount += 8;
                }
            }
        }
    }

    public static final int changeAlpha(int origColor, final int userInputedAlpha) {
        origColor &= 0xFFFFFF;
        return userInputedAlpha << 24 | origColor;
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public void glColor(final int red, final int green, final int blue, final int alpha) {
        GlStateManager.color(red / 255.0f, green / 255.0f, blue / 255.0f, alpha / 255.0f);
    }

    public void glColor(final Color color) {
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        final float alpha = color.getAlpha() / 255.0f;
        GlStateManager.color(red, green, blue, alpha);
    }

    private void glColor(final int hex) {
        final float alpha = (hex >> 24 & 0xFF) / 255.0f;
        final float red = (hex >> 16 & 0xFF) / 255.0f;
        final float green = (hex >> 8 & 0xFF) / 255.0f;
        final float blue = (hex & 0xFF) / 255.0f;
        GlStateManager.color(red, green, blue, alpha);
    }

    public void resetCaps() {
        this.glCapMap.forEach(this::setGlState);
    }

    public void enableGlCap(final int cap) {
        this.setGlCap(cap, true);
    }

    public void enableGlCap(final int... caps) {
        for (final int cap : caps) {
            this.setGlCap(cap, true);
        }
    }

    public void disableGlCap(final int cap) {
        this.setGlCap(cap, false);
    }

    public void disableGlCap(final int... caps) {
        for (final int cap : caps) {
            this.setGlCap(cap, false);
        }
    }

    public void setGlCap(final int cap, final boolean state) {
        this.glCapMap.put(cap, GL11.glGetBoolean(cap));
        this.setGlState(cap, state);
    }

    public void setGlState(final int cap, final boolean state) {
        if (state) {
            GL11.glEnable(cap);
        }
        else {
            GL11.glDisable(cap);
        }
    }

    public void drawOutlineLine(double left, double top, double right, double bottom, final float width, final int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(width);
        if (left < right) {
            final double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            final double j = top;
            top = bottom;
            bottom = j;
        }
        float a1 = 0.0f;
        float r1 = 0.0f;
        float g1 = 0.0f;
        float b1 = 0.0f;
        float a2 = 0.0f;
        float r2 = 0.0f;
        float g2 = 0.0f;
        float b2 = 0.0f;
        float a3 = 0.0f;
        float r3 = 0.0f;
        float g3 = 0.0f;
        float b3 = 0.0f;
        float a4;
        float r4;
        float g4;
        float b4;
        if (this.outlineMode.getValue() == OutLineMode.RAINBOW) {
            final int rainbow = rainbow(1).getRGB();
            final int rainbow2 = rainbow(1000).getRGB();
            final int rainbow3 = rainbow(500).getRGB();
            final int rainbow4 = rainbow(1).getRGB();
            a4 = (rainbow >> 24 & 0xFF) / 255.0f;
            r4 = (rainbow >> 16 & 0xFF) / 255.0f;
            g4 = (rainbow >> 8 & 0xFF) / 255.0f;
            b4 = (rainbow & 0xFF) / 255.0f;
            a1 = (rainbow2 >> 24 & 0xFF) / 255.0f;
            r1 = (rainbow2 >> 16 & 0xFF) / 255.0f;
            g1 = (rainbow2 >> 8 & 0xFF) / 255.0f;
            b1 = (rainbow2 & 0xFF) / 255.0f;
            a2 = (rainbow3 >> 24 & 0xFF) / 255.0f;
            r2 = (rainbow3 >> 16 & 0xFF) / 255.0f;
            g2 = (rainbow3 >> 8 & 0xFF) / 255.0f;
            b2 = (rainbow3 & 0xFF) / 255.0f;
            a3 = (rainbow4 >> 24 & 0xFF) / 255.0f;
            r3 = (rainbow4 >> 16 & 0xFF) / 255.0f;
            g3 = (rainbow4 >> 8 & 0xFF) / 255.0f;
            b3 = (rainbow4 & 0xFF) / 255.0f;
        }
        else if (this.outlineMode.getValue() == OutLineMode.RAINBOW2) {
            final int rainbow = rainbow(1).getRGB();
            final int rainbow2 = rainbow(1000).getRGB();
            a4 = (rainbow >> 24 & 0xFF) / 255.0f;
            r4 = (rainbow >> 16 & 0xFF) / 255.0f;
            g4 = (rainbow >> 8 & 0xFF) / 255.0f;
            b4 = (rainbow & 0xFF) / 255.0f;
            a1 = (rainbow2 >> 24 & 0xFF) / 255.0f;
            r1 = (rainbow2 >> 16 & 0xFF) / 255.0f;
            g1 = (rainbow2 >> 8 & 0xFF) / 255.0f;
            b1 = (rainbow2 & 0xFF) / 255.0f;
        }
        else {
            a4 = (color >> 24 & 0xFF) / 255.0f;
            r4 = (color >> 16 & 0xFF) / 255.0f;
            g4 = (color >> 8 & 0xFF) / 255.0f;
            b4 = (color & 0xFF) / 255.0f;
        }
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        if (this.outlineMode.getValue() == OutLineMode.RAINBOW) {
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(right, bottom, 0.0).color(r1, g1, b1, a1).endVertex();
            bufferbuilder.pos(right, top, 0.0).color(r1, g1, b1, a1).endVertex();
            bufferbuilder.pos(left, top, 0.0).color(r2, g2, b2, a2).endVertex();
            bufferbuilder.pos(left, bottom, 0.0).color(r3, g3, b3, a3).endVertex();
        }
        else if (this.outlineMode.getValue() == OutLineMode.DEPEND) {
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(right, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(right, top, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(left, top, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
        }
        else if (this.outlineMode.getValue() == OutLineMode.NORMAL) {
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(right, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(right, top, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(left, top, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
        }
        else if (this.outlineMode.getValue() == OutLineMode.RAINBOW2) {
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(right, bottom, 0.0).color(r1, g1, b1, a1).endVertex();
            bufferbuilder.pos(right, top, 0.0).color(r4, g4, b4, a4).endVertex();
            bufferbuilder.pos(left, top, 0.0).color(r1, g1, b1, a1).endVertex();
            bufferbuilder.pos(left, bottom, 0.0).color(r4, g4, b4, a4).endVertex();
        }
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static Color rainbow(final int delay) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState %= 360.0;
        return Color.getHSBColor((float)(rainbowState / 360.0), 1.0f, 1.0f);
    }
}