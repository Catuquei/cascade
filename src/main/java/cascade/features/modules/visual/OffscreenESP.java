package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render2DEvent;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import cascade.util.Util;
import com.google.common.collect.Maps;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;

import java.util.Map;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.GL11;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.Display;
import net.minecraft.util.math.MathHelper;
import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.player.EntityPlayer;

public class OffscreenESP extends Module {

    public OffscreenESP() {
        super("OffscreenESP", Category.VISUAL, "Shows the direction players are in");
    }

    public Setting<Boolean> onlyFriends = register(new Setting("OnlyFriends", false));
    private final Setting<Color> c = register(new Setting("Color", new Color(-1)));
    private final Setting<Integer> radius = register(new Setting("Radius", 45, 1, 200));
    private final Setting<Float> size = register(new Setting("Size", 10.0f, 0.1f, 25.0f));
    private final Setting<Boolean> outline = register(new Setting("Outline", true));
    private final Setting<Float> outlineWidth = register(new Setting("Width", 1.0f,0.1f, 3.0f, v -> outline.getValue()));
    private final Setting<Integer> fadeDistance = register(new Setting("FadeDistance", 100, 1, 200));
    private final EntityListener entityListener = new EntityListener();

    @Override
    public void onRender2D(Render2DEvent event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        entityListener.render();
        mc.world.loadedEntityList.forEach(o -> {
            if (o instanceof EntityPlayer && isValid((EntityPlayer) o)) {
                EntityPlayer entity = (EntityPlayer) o;
                Vec3d pos = entityListener.getEntityLowerBounds().get(entity);
                if (pos != null && !isOnScreen(pos) && !RenderUtil.isInViewFrustrum(entity)) {
                    Color color = ColorUtil.getColor(entity, c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue(), (int) MathHelper.clamp(255.0f - 255.0f / (float) fadeDistance.getValue() * mc.player.getDistance(entity), 100.0f, 255.0f), !onlyFriends.getValue());
                    int x = Display.getWidth() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                    int y = Display.getHeight() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
                    float yaw = getRotations(entity) - mc.player.rotationYaw;
                    GL11.glTranslatef((float) x, (float) y, 0.0f);
                    GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                    GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
                    RenderUtil.drawTracerPointer(x, y - radius.getValue(), size.getValue(), 2.0f, 1.0f, outline.getValue(), outlineWidth.getValue(), color.getRGB());
                    GL11.glTranslatef((float) x, (float) y, 0.0f);
                    GL11.glRotatef(-yaw, 0.0f, 0.0f, 1.0f);
                    GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
                }
            }
        });
    }


    private boolean isOnScreen(Vec3d pos) {
        if (!(pos.x > -1.0)) return false;
        if (!(pos.y < 1.0)) return false;
        if (!(pos.x > -1.0)) return false;
        if (!(pos.z < 1.0)) return false;
        int n = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        if (!(pos.x / (double) n >= 0.0)) return false;
        int n2 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        if (!(pos.x / (double) n2 <= (double) Display.getWidth())) return false;
        int n3 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        if (!(pos.y / (double) n3 >= 0.0)) return false;
        int n4 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        return pos.y / (double) n4 <= (double) Display.getHeight();
    }

    private boolean isValid(EntityPlayer entity) {
        if (entity != mc.player && !entity.isInvisible()) {
            if (Cascade.friendManager.isFriend(entity) && onlyFriends.getValue()) {
                return true;
            }
            if (!onlyFriends.getValue()) {
                return true;
            }
        }
        return false;
    }

    private float getRotations(EntityLivingBase ent) {
        double x = ent.posX - mc.player.posX;
        double z = ent.posZ - mc.player.posZ;
        return (float) (-(Math.atan2(x, z) * 57.29577951308232));
    }

    private static class EntityListener {
        private final Map<Entity, Vec3d> entityUpperBounds = Maps.newHashMap();
        private final Map<Entity, Vec3d> entityLowerBounds = Maps.newHashMap();

        private EntityListener() {
        }

        private void render() {
            if (!this.entityUpperBounds.isEmpty()) {
                this.entityUpperBounds.clear();
            }
            if (!this.entityLowerBounds.isEmpty()) {
                this.entityLowerBounds.clear();
            }
            for (Entity e : Util.mc.world.loadedEntityList) {
                Vec3d bound = this.getEntityRenderPosition(e);
                bound.add(new Vec3d(0.0, (double) e.height + 0.2, 0.0));
                Vec3d upperBounds = RenderUtil.to2D(bound.x, bound.y, bound.z);
                Vec3d lowerBounds = RenderUtil.to2D(bound.x, bound.y - 2.0, bound.z);
                if (upperBounds == null || lowerBounds == null) continue;
                this.entityUpperBounds.put(e, upperBounds);
                this.entityLowerBounds.put(e, lowerBounds);
            }
        }

        private Vec3d getEntityRenderPosition(Entity entity) {
            double partial = Util.mc.timer.renderPartialTicks;
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - Util.mc.getRenderManager().viewerPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - Util.mc.getRenderManager().viewerPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - Util.mc.getRenderManager().viewerPosZ;
            return new Vec3d(x, y, z);
        }

        public Map<Entity, Vec3d> getEntityLowerBounds() {
            return this.entityLowerBounds;
        }
    }
}