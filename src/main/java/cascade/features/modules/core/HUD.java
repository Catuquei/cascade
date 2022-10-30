package cascade.features.modules.core;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.features.Feature;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.manager.ServerManager;
import cascade.util.core.TextUtil;
import cascade.util.entity.EntityUtil;
import cascade.util.misc.MathUtil;
import cascade.util.player.InventoryUtil;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentString;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class HUD extends Module {

    public HUD() {
        super("HUD", Category.CORE, "Clients HUD");
        setInstance();
    }

    //todo radar

    private static HUD INSTANCE = new HUD();
    Setting<Boolean> renderingUp = register(new Setting("RenderingUp", false));
    Setting<Boolean> coords = register(new Setting("Coords", false));
    Setting<Boolean> simpleCoords = register(new Setting("SimpleCoords", false, v -> coords.getValue()));
    Setting<Boolean> direction = register(new Setting("Direction", false));
    Setting<Boolean> totems = register(new Setting("Totems", false));
    Setting<Boolean> armor = register(new Setting("Armor", false));
    Setting<Boolean> armorCount = register(new Setting("ArmorCount", false, v -> armor.getValue()));
    Setting<Boolean> defaultArmor = register(new Setting("Default", false, v -> armor.getValue()));
    Setting<Color> armorColorFrom = register(new Setting("ArmorColorFrom", new Color(0xFF7800FF), v -> armor.getValue()));
    Setting<Color> armorColorTo = register(new Setting("ArmorColorTo",new Color(0xFF7800FF), v -> armor.getValue()));
    Setting<Boolean> potions = register(new Setting("Potions", false));

    Setting<Boolean> watermark = register(new Setting("Watermark", false));
    Setting<String> watermarkText = register(new Setting("WatermarkText", Cascade.MODNAME + " " + Cascade.MODVER, v -> watermark.getValue()));
    Setting<Integer> watermarkY = register(new Setting("WatermarkY", 2, 0, 20, v -> watermark.getValue()));

    Setting<Boolean> arrayList = register(new Setting("ArrayList", true));
    Setting<Integer> arrayListY = register(new Setting("ArrayListY", 0, 0, 50, v -> arrayList.getValue()));
    public Setting<Ordering> ordering = register(new Setting("Ordering", Ordering.Length, v -> arrayList.getValue()));
    public enum Ordering {Length, Alphabet}

    Setting<Boolean> pvpInfo = register(new Setting("PvpInfo", false));
    Setting<String> pvpText = register(new Setting("PvpText", "Cascade", v -> pvpInfo.getValue()));

    Setting<Welcomer> welcomer = register(new Setting("Welcomer", Welcomer.None));
    enum Welcomer {None, Custom, Calendar}
    Setting<String> welcomerText = register(new Setting("WelcomerText", "UID:-1", v -> welcomer.getValue() == Welcomer.Custom));

    Setting<Boolean> ping = register(new Setting("Ping", false));
    Setting<Boolean> tps = register(new Setting("TPS", false));
    Setting<Boolean> fps = register(new Setting("FPS", false));
    Setting<Boolean> time = register(new Setting("Time", false));
    Setting<Boolean> lagFactor = register(new Setting("LagFactor", false));
    Setting<Boolean> speed = register(new Setting("Speed", false));
    Setting<Integer> speedTicks = register(new Setting("Ticks", 20, 5, 100, v -> speed.getValue()));
    Setting<TextUtil.Color> infoColor = register(new Setting("InfoColor", TextUtil.Color.GRAY, v -> ping.getValue() || tps.getValue() || fps.getValue() || time.getValue() || speed.getValue()));

    Setting<Boolean> lagNotify = register(new Setting("LagNotify", false));
    //todo turn lagtime into lagticks
    public Setting<Integer> lagTime = register(new Setting("LagTime", 1000, 0, 5000, v -> lagNotify.getValue()));
    static ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
    ArrayDeque<Double> speedDeque = new ArrayDeque<>();
    int color;

    public static HUD getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HUD();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public void onRender2D(Render2DEvent event) {
        if (Feature.fullNullCheck()) {
            return;
        }
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        color = ColorUtil.toRGBA(ClickGui.getInstance().c.getValue().getRed(), ClickGui.getInstance().c.getValue().getGreen(), ClickGui.getInstance().c.getValue().getBlue());
        if (pvpInfo.getValue()) {
            if ((ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(pvpText.getValue(), 2.0F, 250, ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = pvpText.getValue().toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 2.0F + f, 250, ColorUtil.rainbow(arrayOfInt[0] * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(pvpText.getValue(), 2.0F, 250, color, true);
            }

            //tots
            int totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> (itemStack.getItem() == Items.TOTEM_OF_UNDYING)).mapToInt(ItemStack::getCount).sum() + (InventoryUtil.heldItem(Items.TOTEM_OF_UNDYING, InventoryUtil.Hand.Off) ? 1 : 0);
            renderer.drawString(totems == 0 ? ChatFormatting.RED + "" + totems : ChatFormatting.GREEN + "" +  totems, 2, 260, ColorUtil.rainbow(((ClickGui.getInstance()).rainbowHue.getValue())).getRGB(), true);

            //ping
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
            renderer.drawString(pingString, 2.0F, 270, color, true);

        }
        if (watermark.getValue()) {
            if ((ClickGui.getInstance()).rainbow.getValue()) {
                if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                    renderer.drawString(watermarkText.getValue(), 2.0F, watermarkY.getValue(), ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                } else {
                    int[] arrayOfInt = {1};
                    char[] stringToCharArray = watermarkText.getValue().toCharArray();
                    float f = 0.0F;
                    for (char c : stringToCharArray) {
                        renderer.drawString(String.valueOf(c), 2.0F + f, watermarkY.getValue(), ColorUtil.rainbow(arrayOfInt[0] * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB(), true);
                        f += renderer.getStringWidth(String.valueOf(c));
                        arrayOfInt[0] = arrayOfInt[0] + 1;
                    }
                }
            } else {
                renderer.drawString(watermarkText.getValue(), 2.0F, watermarkY.getValue(), color, true);
            }
        }
        int[] counter1 = {1};
        int j = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && !renderingUp.getValue()) ? 14 : 0;
        if (arrayList.getValue()) {
            if (renderingUp.getValue()) {
                if (ordering.getValue() == Ordering.Alphabet) {
                    for (int k = 0; k < Cascade.moduleManager.sortedModulesABC.size(); k++) {
                        String str = Cascade.moduleManager.sortedModulesABC.get(k);
                        this.renderer.drawString(str, (width - 2 - this.renderer.getStringWidth(str)), ((2 + j * 10) + arrayListY.getValue()), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                        j++;
                        counter1[0] = counter1[0] + 1;
                    }
                } else {
                    for (int k = 0; k < Cascade.moduleManager.sortedModules.size(); k++) {
                        Module module = Cascade.moduleManager.sortedModules.get(k);
                        String str = module.getName() + ChatFormatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]") : "");
                        this.renderer.drawString(str, (width - 2 - this.renderer.getStringWidth(str)), ((2 + j * 10) + arrayListY.getValue()), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                        j++;
                        counter1[0] = counter1[0] + 1;
                    }
                }
            } else if (ordering.getValue() == Ordering.Alphabet) {
                for (int k = 0; k < Cascade.moduleManager.sortedModulesABC.size(); k++) {
                    String str = Cascade.moduleManager.sortedModulesABC.get(k);
                    j += 10;
                    this.renderer.drawString(str, (width - 2 - this.renderer.getStringWidth(str)), ((height - j) + arrayListY.getValue()), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
            } else {
                for (int k = 0; k < Cascade.moduleManager.sortedModules.size(); k++) {
                    Module module = Cascade.moduleManager.sortedModules.get(k);
                    String str = module.getName() + ChatFormatting.GRAY + ((module.getDisplayInfo() != null) ? (" [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]") : "");
                    j += 10;
                    this.renderer.drawString(str, (width - 2 - this.renderer.getStringWidth(str)), ((height - j) + arrayListY.getValue()), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
            }
        }
        int i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat && this.renderingUp.getValue().booleanValue()) ? 13 : (this.renderingUp.getValue().booleanValue() ? -2 : 0);
        if (renderingUp.getValue()) {
            if (potions.getValue()) {
                java.util.List<PotionEffect> effects = new ArrayList<>(mc.player.getActivePotionEffects());
                for (PotionEffect potionEffect : effects) {
                    String str = Cascade.potionManager.getColoredPotionString(potionEffect);
                    i += 10;
                    renderer.drawString(str, (width - renderer.getStringWidth(str) - 2), (height - 2 - i), potionEffect.getPotion().getLiquidColor(), true);
                }
            }
            if (lagFactor.getValue()) {
                String str = "LagFactor " + TextUtil.coloredString(String.valueOf(Cascade.serverManager.getLagFactor()), infoColor.getValue());
                i += 10;
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (speed.getValue()) {
                double speed = calcSpeed(mc.player);
                double displaySpeed = speed;
                if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) {
                    speedDeque.add(speed);
                } else {
                    speedDeque.pollFirst();
                }

                while (!speedDeque.isEmpty() && speedDeque.size() > speedTicks.getValue()) {
                    speedDeque.poll();
                }

                displaySpeed = average(speedDeque);
                String str = "Speed " + TextUtil.coloredString(String.format("%.1f", displaySpeed) + " km/h", infoColor.getValue());
                i += 10;
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (time.getValue()) {

                String str = "Time " + TextUtil.coloredString(String.format((new SimpleDateFormat("h:mm a")).format(new Date())), infoColor.getValue());
                i += 10;
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (this.tps.getValue()) {
                String str = "TPS " + TextUtil.coloredString(String.format(String.valueOf(Cascade.serverManager.getTPS())), infoColor.getValue());
                i += 10;
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            String fpsText =  "FPS " + TextUtil.coloredString(String.format(String.valueOf(Minecraft.debugFPS)), infoColor.getValue());
            String str1 = "Ping " + TextUtil.coloredString(String.format(String.valueOf(Cascade.serverManager.getPing())), infoColor.getValue());
            if (this.renderer.getStringWidth(str1) > this.renderer.getStringWidth(fpsText)) {
                if (this.ping.getValue()) {
                    i += 10;
                    this.renderer.drawString(str1, (width - this.renderer.getStringWidth(str1) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (this.fps.getValue().booleanValue()) {
                    i += 10;
                    this.renderer.drawString(fpsText, (width - this.renderer.getStringWidth(fpsText) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
            } else {
                if (this.fps.getValue().booleanValue()) {
                    i += 10;
                    this.renderer.drawString(fpsText, (width - this.renderer.getStringWidth(fpsText) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (this.ping.getValue().booleanValue()) {
                    i += 10;
                    this.renderer.drawString(str1, (width - this.renderer.getStringWidth(str1) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
            }
        } else {
            if (potions.getValue()) {
                List<PotionEffect> effects = new ArrayList<>(mc.player.getActivePotionEffects());
                for (PotionEffect potionEffect : effects) {
                    String str = Cascade.potionManager.getColoredPotionString(potionEffect);
                    renderer.drawString(str, (width - renderer.getStringWidth(str) - 2), (2 + i++ * 10), potionEffect.getPotion().getLiquidColor(), true);
                }
            }
            if (lagFactor.getValue()) {
                String str = "LagFactor " + TextUtil.coloredString(String.valueOf(Cascade.serverManager.getLagFactor()), infoColor.getValue());
                i += 10;
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (height - 2 - i), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (speed.getValue()) {
                double speed = calcSpeed(mc.player);
                double displaySpeed = speed;
                if (speed > 0.0 || mc.player.ticksExisted % 4 == 0) {
                    speedDeque.add(speed);
                } else {
                    speedDeque.pollFirst();
                }

                while (!speedDeque.isEmpty() && speedDeque.size() > speedTicks.getValue()) {
                    speedDeque.poll();
                }

                displaySpeed = average(speedDeque);
                String str = "Speed " + TextUtil.coloredString(String.format("%.1f", displaySpeed) + " km/h", infoColor.getValue());
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (this.time.getValue()) {
                String str = "Time " + TextUtil.coloredString(String.format((new SimpleDateFormat("h:mm a")).format(new Date())), infoColor.getValue());
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            if (this.tps.getValue()) {
                String str = "TPS " + TextUtil.coloredString(String.format(Cascade.serverManager.getTPS() + ""), infoColor.getValue());
                this.renderer.drawString(str, (width - this.renderer.getStringWidth(str) - 2), (2 + i++ * 10),(ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                counter1[0] = counter1[0] + 1;
            }
            String fpsText = "FPS " + TextUtil.coloredString(String.format(Minecraft.debugFPS + ""), infoColor.getValue());
            String str1 = "Ping " + TextUtil.coloredString(String.format(Cascade.serverManager.getPing() + ""), infoColor.getValue());
            if (renderer.getStringWidth(str1) > renderer.getStringWidth(fpsText)) {
                if (ping.getValue()) {
                    this.renderer.drawString(str1, (width - this.renderer.getStringWidth(str1) - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (this.fps.getValue()) {
                    this.renderer.drawString(fpsText, (width - this.renderer.getStringWidth(fpsText) - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
            } else {
                if (this.fps.getValue().booleanValue()) {
                    this.renderer.drawString(fpsText, (width - this.renderer.getStringWidth(fpsText) - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
                if (this.ping.getValue().booleanValue()) {
                    this.renderer.drawString(str1, (width - this.renderer.getStringWidth(str1) - 2), (2 + i++ * 10), (ClickGui.getInstance()).rainbow.getValue().booleanValue() ? (((ClickGui.getInstance()).rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up) ? ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB() : ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB()) : this.color, true);
                    counter1[0] = counter1[0] + 1;
                }
            }
        }
        boolean inHell = mc.world.getBiome(mc.player.getPosition()).getBiomeName() == "Hell";
        int posX = (int) mc.player.posX;
        int posY = (int) mc.player.posY;
        int posZ = (int) mc.player.posZ;
        float nether = !inHell ? 0.125F : 8.0F;
        int hposX = (int) (mc.player.posX * nether);
        int hposZ = (int) (mc.player.posZ * nether);
        i = (mc.currentScreen instanceof net.minecraft.client.gui.GuiChat) ? 14 : 0;
        String coordinates = ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]") : (posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]"));
        String direction = this.direction.getValue() ? ChatFormatting.WHITE + Cascade.rotationManager.getDirection4D(false) : "";
        String coords = this.coords.getValue() ? coordinates : "";
        String simpleCoord = this.coords.getValue() ? ChatFormatting.WHITE + (inHell ? (posX + " " + posY + " " + posZ) : (posX + " " + posY + " " + posZ)) : "";
        i += 10;
        if ((ClickGui.getInstance()).rainbow.getValue()) {
            String rainbowCoords = this.coords.getValue() ? ("XYZ " + (inHell ? (posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]") : (posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]"))) : "";
            if ((ClickGui.getInstance()).rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                this.renderer.drawString(direction, 2.0F, (height - i - 11), ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB(), true);
                this.renderer.drawString(simpleCoords.getValue() ? simpleCoord : rainbowCoords, 2.0F, (height - i), ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB(), true);
            } else {
                int[] counter2 = {1};
                char[] stringToCharArray = direction.toCharArray();
                float s = 0.0F;
                for (char c : stringToCharArray) {
                    this.renderer.drawString(String.valueOf(c), 2.0F + s, (height - i - 11), ColorUtil.rainbow(counter2[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB(), true);
                    s += this.renderer.getStringWidth(String.valueOf(c));
                    counter2[0] = counter2[0] + 1;
                }
                int[] counter3 = {1};
                char[] stringToCharArray2 = rainbowCoords.toCharArray();
                float u = 0.0F;
                for (char c : stringToCharArray2) {
                    this.renderer.drawString(String.valueOf(c), 2.0F + u, (height - i), ColorUtil.rainbow(counter3[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB(), true);
                    u += this.renderer.getStringWidth(String.valueOf(c));
                    counter3[0] = counter3[0] + 1;
                }
            }
        } else {
            this.renderer.drawString(direction, 2.0F, (height - i - 11), this.color, true);
            this.renderer.drawString(simpleCoords.getValue() ? simpleCoord : coords, 2.0F, (height - i), this.color, true);
        }
        if (armor.getValue()) {
            renderArmor(true, armorCount.getValue());
        }
        if (totems.getValue()) {
            renderTotem();
        }
        if (welcomer.getValue() != Welcomer.None) {
            renderWelcomer();
        }
        if (lagNotify.getValue()) {
            renderLag();
        }
    }

    void renderArmor(boolean percent, boolean amount) {
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        GlStateManager.enableTexture2D();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - ((EntityUtil.isInLiquid() && mc.playerController.gameIsSurvivalOrAdventure()) ? 10 : 0);
        for (ItemStack is : mc.player.inventory.armorInventory) {
            ++iteration;
            if (is.isEmpty()) {
                continue;
            }
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200.0f;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0.0f;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = (is.getCount() > 1) ? (is.getCount() + "") : "";
            renderer.drawStringWithShadow(s, (float) (x + 19 - 2 - renderer.getStringWidth(s)), (float) (y + 9), 16777215);
            if (percent) {
                int dmg;
                float from = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float to = 1.0f - from;
                dmg = 100 - (int) (to * 100.0f);
                if (from > 1.0f) {
                    from = 1.0f;
                } else if (from < 0.0f) {
                    from = 0.0f;
                }

                if (to > 1.0f) {
                    to = 1.0f;
                }
                if (dmg < 0) {
                    dmg = 0;
                }
                float red = defaultArmor.getValue() ? to * 255 : armorColorFrom.getValue().getRed() * from - armorColorTo.getValue().getRed();
                float green = defaultArmor.getValue() ? from * 255 : armorColorFrom.getValue().getGreen() * from - armorColorTo.getValue().getGreen();
                float blue = defaultArmor.getValue() ? 0 : armorColorFrom.getValue().getBlue() * from - armorColorTo.getValue().getBlue();
                if (red < 0) {
                    red *= -1.0f;
                }
                if (green < 0) {
                    green *= -1.0f;
                }
                if (blue < 0) {
                    blue *= -1.0f;
                }
                renderer.drawStringWithShadow(dmg + "", (float) (x + 8 - renderer.getStringWidth(dmg + "") / 2), (float) (y - 8), ColorUtil.toRGBA((int) red, (int) green, (int) blue));
            }
            if (amount) {
                int a = mc.player.inventory.mainInventory.stream().filter(itemStack -> (itemStack.getItem() == is.getItem())).mapToInt(ItemStack::getCount).sum();
                renderer.drawStringWithShadow(a == 0 ? "" : String.valueOf(a), (float) (x + 13 - renderer.getStringWidth(s)), (float) (y + 9), 16777215);
            }
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }


    void renderTotem() {
        int width = renderer.scaledWidth;
        int height = renderer.scaledHeight;
        int totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> (itemStack.getItem() == Items.TOTEM_OF_UNDYING)).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            totems += mc.player.getHeldItemOffhand().getCount();
        }
        if (totems > 0) {
            GlStateManager.enableTexture2D();
            int i = width / 2;
            int y = height - 55 - ((mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure()) ? 10 : 0);
            int x = i - 189 + 180 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200.0F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(totem, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, totem, x, y, "");
            RenderUtil.itemRender.zLevel = 0.0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(totems + "", (x + 19 - 2 - renderer.getStringWidth(totems + "")), (y + 9), 16777215);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }

    void renderLag() {
        int width = renderer.scaledWidth;
        if (Cascade.serverManager.isServerNotResponding(lagTime.getValue())) {
            String text = ChatFormatting.RED + "Server not responding " + MathUtil.round((float) Cascade.serverManager.serverRespondingTime() / 1000.0F, 1) + "s.";
            renderer.drawString(text, width / 2.0f - renderer.getStringWidth(text) / 2.0f + 2.0f, 20.0f, color, true);
        }
    }

    void renderWelcomer() {
        int width = renderer.scaledWidth;
        String text = "";
        switch (welcomer.getValue()) {
            case None: {
                text = "";
            }
            case Custom: {
                text = welcomerText.getValue();
                break;
            }
            case Calendar: {
                text = MathUtil.getTimeOfDay() + mc.player.getDisplayNameString();
                break;
            }
        }
        if (ClickGui.getInstance().rainbow.getValue()) {
            if (ClickGui.getInstance().rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
                this.renderer.drawString(text, width / 2.0F - renderer.getStringWidth(text) / 2.0F + 2.0F, 2.0F, ColorUtil.rainbow((ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB(), true);
            } else {
                int[] counter1 = {1};
                char[] stringToCharArray = text.toCharArray();
                float i = 0.0F;
                for (char c : stringToCharArray) {
                    this.renderer.drawString(String.valueOf(c), width / 2.0F - this.renderer.getStringWidth(text) / 2.0F + 2.0F + i, 2.0F, ColorUtil.rainbow(counter1[0] * (ClickGui.getInstance()).rainbowHue.getValue().intValue()).getRGB(), true);
                    i += this.renderer.getStringWidth(String.valueOf(c));
                    counter1[0] = counter1[0] + 1;
                }
            }
        } else {
            this.renderer.drawString(text, width / 2.0F - this.renderer.getStringWidth(text) / 2.0F + 2.0F, 2.0F, this.color, true);
        }
    }

    @Override
    public void onDisable() {
        speedDeque.clear();
    }

    double calcSpeed(EntityPlayerSP player) {
        double tps = 1000.0 / mc.timer.tickLength;
        double x = player.posX - player.prevPosX;
        double z = player.posZ - player.prevPosZ;
        double speed = Math.hypot(x, z) * tps;
        speed *= 3.6;

        return speed;
    }

    double average(Collection<Double> collection) {
        if (collection.isEmpty()) return 0.0;

        double sum = 0.0;
        int size = 0;

        for (double element : collection) {
            sum += element;
            size++;
        }

        return sum / size;
    }
}