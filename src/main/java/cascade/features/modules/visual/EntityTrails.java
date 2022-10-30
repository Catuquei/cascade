package cascade.features.modules.visual;

import cascade.Cascade;
import cascade.event.events.Render3DEvent;
import cascade.features.modules.Module;
import cascade.features.setting.Setting;
import cascade.util.render.ColorUtil;
import cascade.util.render.RenderUtil;
import cascade.util.misc.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EntityTrails extends Module {

    public Setting<Boolean> players = register(new Setting<>("Players", false));
    public Setting<Float> lineWidth = register(new Setting<>("LineWidth", 2.0f, 0.1f, 5.0f, v -> players.getValue()));
    public Setting<Boolean> fade = register(new Setting<>("Fade", false, v -> players.getValue()));
    public Setting<Integer> removeDelay = register(new Setting<>("RemoveDelay", 1000, 0, 2000, v -> players.getValue()));
    public Setting<Color> startColor = register(new Setting<>("StartColor", new Color(-1), v -> players.getValue()));
    public Setting<Color> endColor = register(new Setting<>("EndColor", new Color(-1), v -> players.getValue()));

    public Setting<Boolean> pearls = register(new Setting<>("Pearls", false));
    public Setting<Color> pearlColor = register(new Setting<>("PearlColor", new Color(-1), v -> pearls.getValue()));
    public Setting<Float> pearlLineWidth = register(new Setting<>("PearlLineWidth", 3.0f, 0.0f, 10.0f, v -> pearls.getValue()));

    HashMap<UUID, List<Vec3d>> pearlPos = new HashMap<>();
    HashMap<UUID, Double> removeWait = new HashMap<>();
    Map<UUID, ItemTrail> trails = new HashMap<>();

    public EntityTrails() {
        super("EntityTrails", Category.VISUAL, "Draws a line behind entities (Breadcrumbs)");
    }

    @Override
    public void onUpdate() {
        if (pearls.getValue()) {
            UUID pearlPos = null;
            for (UUID uuid : removeWait.keySet())
                if (removeWait.get(uuid) <= 0) {
                    this.pearlPos.remove(uuid);
                    pearlPos = uuid;
                } else
                    removeWait.replace(uuid, removeWait.get(uuid) - 0.05);
            if (pearlPos != null)
                removeWait.remove(pearlPos);
            for (Entity e : mc.world.getLoadedEntityList()) {
                if (!(e instanceof EntityEnderPearl))
                    continue;
                if (!this.pearlPos.containsKey(e.getUniqueID())) {
                    this.pearlPos.put(e.getUniqueID(), new ArrayList<>(Collections.singletonList(e.getPositionVector())));
                    this.removeWait.put(e.getUniqueID(), 0.1);
                } else {
                    this.removeWait.replace(e.getUniqueID(), 0.1);
                    List<Vec3d> v = this.pearlPos.get(e.getUniqueID());
                    v.add(e.getPositionVector());
                }
            }
        }
    }

    public void onTick() {
        if (players.getValue()) {
            if (fullNullCheck() || isDisabled()) {
                return;
            }
            if (trails.containsKey(mc.player.getUniqueID())) {
                final ItemTrail playerTrail = trails.get(mc.player.getUniqueID());
                playerTrail.timer.reset();
                final List<Position> toRemove = playerTrail.positions.stream().filter(position -> System.currentTimeMillis() - position.time > removeDelay.getValue().longValue()).collect(Collectors.toList());
                playerTrail.positions.removeAll(toRemove);
                playerTrail.positions.add(new Position(mc.player.getPositionVector()));
            } else
                trails.put(mc.player.getUniqueID(), new ItemTrail(mc.player));
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (fullNullCheck() || isDisabled()) {
            return;
        }
        if (players.getValue()) {
            trails.forEach((key, value) -> {
                if (value.entity.isDead || mc.world.getEntityByID(value.entity.getEntityId()) == null) {
                    if (value.timer.isPaused())
                        value.timer.reset();

                    value.timer.setPaused(false);
                }
                if (!value.timer.isPassed())
                    drawTrail(value);

            });
        }
        if (pearlPos.isEmpty() || !pearls.getValue())
            return;
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(pearlLineWidth.getValue());
        pearlPos.keySet().stream().filter(uuid -> pearlPos.get(uuid).size() > 2).forEach(uuid -> {
            GL11.glBegin(1);
            IntStream.range(1, pearlPos.get(uuid).size()).forEach(i -> {
                Color color = pearlColor.getValue();
                GL11.glColor3d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
                List<Vec3d> pos = pearlPos.get(uuid);
                GL11.glVertex3d(pos.get(i).x - mc.getRenderManager().viewerPosX, pos.get(i).y - mc.getRenderManager().viewerPosY, pos.get(i).z - mc.getRenderManager().viewerPosZ);
                GL11.glVertex3d(pos.get(i - 1).x - mc.getRenderManager().viewerPosX, pos.get(i - 1).y - mc.getRenderManager().viewerPosY, pos.get(i - 1).z - mc.getRenderManager().viewerPosZ);
            });
            GL11.glEnd();
        });
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    void drawTrail(final ItemTrail trail) {
        final Color fadeColor = endColor.getValue();
        RenderUtil.prepare();
        GL11.glLineWidth(lineWidth.getValue());
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        (RenderUtil.builder = RenderUtil.tessellator2.getBuffer()).begin(3, DefaultVertexFormats.POSITION_COLOR);
        buildBuffer(RenderUtil.builder, trail, startColor.getValue(), fade.getValue() ? fadeColor : startColor.getValue());
        RenderUtil.tessellator2.draw();
        RenderUtil.release();
    }

    void buildBuffer(final BufferBuilder builder, final ItemTrail trail, final Color start, final Color end) {
        for (final Position p : trail.positions) {
            final Vec3d pos = RenderUtil.updateToCamera(p.pos);
            final double value = normalize(trail.positions.indexOf(p), trail.positions.size());
            RenderUtil.addBuilderVertex(builder, pos.x, pos.y, pos.z, ColorUtil.interpolate((float) value, start, end));
        }
    }

    double normalize(final double value, final double max) {
        return (value - 0.0) / (max - 0.0);
    }

    static class ItemTrail {
        public Entity entity;
        public List<Position> positions;
        public Timer timer;

        ItemTrail(Entity entity) {
            this.entity = entity;
            positions = new ArrayList<>();
            (timer = new Timer()).setDelay(1000);
            timer.setPaused(true);
        }
    }

    static class Position {
        public Vec3d pos;
        public long time;

        public Position(Vec3d pos) {
            this.pos = pos;
            time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Position position = (Position) o;
            return time == position.time && Objects.equals(pos, position.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, time);
        }
    }
}