package cascade.features.modules.misc;

import cascade.Cascade;
import cascade.event.events.ConnectionEvent;
import cascade.event.events.Render3DEvent;
import cascade.features.command.Command;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.render.ColorUtil;
import cascade.util.misc.MathUtil;
import cascade.util.render.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogSpots extends Module {

    public LogSpots() {
        super("LogSpots", Module.Category.MISC, "Shows log spots");
        INSTANCE = this;
    }

    Setting<Color> c = register(new Setting("Color", new Color(-1)));
    Setting<Boolean> scale = register(new Setting("Scale", false));
    Setting<Float> size = register(new Setting("Size", 4.0f, 0.1f, 20.0f));
    Setting<Float> factor = register(new Setting("Factor", 0.3f, 0.1f, 1.0f, v -> scale.getValue()));
    Setting<Boolean> smartScale = register(new Setting("SmartScale", false, v -> scale.getValue()));
    Setting<Boolean> rect = register(new Setting("Rectangle", true));
    Setting<Boolean> coords = register(new Setting("Coords", true));
    //Setting<Boolean> notification = register(new Setting("Notification", true));
    Setting<Boolean> message = register(new Setting("Message", false));
    public List<LogoutPos> spots = new CopyOnWriteArrayList<>();
    private static LogSpots INSTANCE;

    public static LogSpots getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LogSpots();
        }
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        spots.clear();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!spots.isEmpty()) {
            synchronized (spots) {
                spots.forEach(spot -> {
                    if (spot.getEntity() != null) {
                        AxisAlignedBB interpolateAxis = RenderUtil.interpolateAxis(spot.getEntity().getEntityBoundingBox());
                        RenderUtil.drawBlockOutline(interpolateAxis, new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha()), 1.0f);
                        double x = interpolate(spot.getEntity().lastTickPosX, spot.getEntity().posX, event.getPartialTicks()) - mc.getRenderManager().renderPosX;
                        double y = interpolate(spot.getEntity().lastTickPosY, spot.getEntity().posY, event.getPartialTicks()) - mc.getRenderManager().renderPosY;
                        double z = interpolate(spot.getEntity().lastTickPosZ, spot.getEntity().posZ, event.getPartialTicks()) - mc.getRenderManager().renderPosZ;
                        renderNameTag(spot.getName(), x, y, z, event.getPartialTicks(), spot.getX(), spot.getY(), spot.getZ());
                    }
                });
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        spots.removeIf(spot -> mc.player.getDistanceSq(spot.getEntity()) >= MathUtil.square(300.0f));
    }

    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        if (event.getStage() == 0) {
            UUID uuid = event.getUuid();
            EntityPlayer entity = mc.world.getPlayerEntityByUUID(uuid);
            if (entity != null && message.getValue() && entity != mc.player) {
                Command.sendMessage("§a" + entity.getName() + " just logged in" + (coords.getValue() ? (" at (" + (int)entity.posX + ", " + (int)entity.posY + ", " + (int)entity.posZ + ")!") : "!"), true, true);
            }
            spots.removeIf(pos -> pos.getName().equalsIgnoreCase(event.getName()));
        } else if (event.getStage() == 1) {
            EntityPlayer entity2 = event.getEntity();
            UUID uuid2 = event.getUuid();
            String name = event.getName();
            if (message.getValue()) {
                Command.sendMessage("§c" + event.getName() + " just logged out" + (coords.getValue() ? (" at (" + (int)entity2.posX + ", " + (int)entity2.posY + ", " + (int)entity2.posZ + ")!") : "!"), true, true);
            }
            if (name != null && entity2 != null && uuid2 != null) {
                spots.add(new LogoutPos(name, uuid2, entity2));
            }
        }
    }

    void renderNameTag(String name, double x, double yi, double z, float delta, double xPos, double yPos, double zPos) {
        double y = yi + 0.7;
        Entity camera = mc.getRenderViewEntity();
        assert camera != null;
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);
        String displayTag = name + " XYZ: " + (int)xPos + ", " + (int)yPos + ", " + (int)zPos;
        double distance = camera.getDistance(x + mc.getRenderManager().viewerPosX, y + mc.getRenderManager().viewerPosY, z + mc.getRenderManager().viewerPosZ);
        int width = this.renderer.getStringWidth(displayTag) / 2;
        double scale = (0.0018 + size.getValue() * (distance * factor.getValue())) / 1000.0;
        if (distance <= 8.0 && smartScale.getValue()) {
            scale = 0.0245;
        }
        if (!this.scale.getValue()) {
            scale = size.getValue() / 100.0;
        }
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.translate((float)x, (float)y + 1.4f, (float)z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableBlend();
        if (this.rect.getValue()) {
            RenderUtil.drawRect((float)(-width - 2), (float)(-(this.renderer.getFontHeight() + 1)), width + 2.0f, 1.5f, 1426063360);
        }
        GlStateManager.disableBlend();
        this.renderer.drawStringWithShadow(displayTag, (float)(-width), (float)(-(this.renderer.getFontHeight() - 1)), ColorUtil.toRGBA(new Color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), c.getValue().getAlpha())));
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
    }

    private double interpolate(double previous, double current, float delta) {
        return previous + (current - previous) * delta;
    }

    private static class LogoutPos {
        private final String name;
        private final UUID uuid;
        private final EntityPlayer entity;
        private final double x;
        private final double y;
        private final double z;

        public LogoutPos(final String name, final UUID uuid, final EntityPlayer entity) {
            this.name = name;
            this.uuid = uuid;
            this.entity = entity;
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
        }

        public String getName() {
            return this.name;
        }

        public UUID getUuid() {
            return this.uuid;
        }

        public EntityPlayer getEntity() {
            return this.entity;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }
}